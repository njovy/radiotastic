/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.udacity.study.jam.radiotastic.test.sync;

import android.content.ContentResolver;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.udacity.study.jam.radiotastic.CategoryItem;
import com.udacity.study.jam.radiotastic.api.RadioApi;
import com.udacity.study.jam.radiotastic.db.category.CategoryColumns;
import com.udacity.study.jam.radiotastic.db.category.CategoryContentValues;
import com.udacity.study.jam.radiotastic.sync.SyncCategoriesCaseImpl;
import com.udacity.study.jam.radiotastic.sync.SyncTask;
import com.udacity.study.jam.radiotastic.test.util.CursorAssert;
import com.udacity.study.jam.radiotastic.test.util.TestResource;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CategoriesSyncTest extends AndroidTestCase {

    private ContentResolver contentResolver;

    private static final Uri CONTENT_URI = CategoryColumns.CONTENT_URI;
    private static final String[] ALL_COLUMNS = CategoryColumns.ALL_COLUMNS;
    private static final String SELECT_BY_CATEGORY_ID =
            CategoryColumns.CATEGORY_ID + "=?";
    private static final String INSERT_BUCKET_SRC = "categories";
    private static final String UPDATE_BUCKET_SRC = "categories_updated";
    private static final String DELETE_BUCKET_SRC = "categories_deleted";

    private RadioApi mockApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache",
                getContext().getCacheDir().getPath());
        mockApi = mock(RadioApi.class);
        contentResolver = getContext().getContentResolver();
        contentResolver.delete(CONTENT_URI, null, null);
    }

    public void testInserts() {
        Collection<CategoryItem> items = createCategories(INSERT_BUCKET_SRC);
        when(mockApi.listPrimaryCategories()).thenReturn(items);

        SyncResult syncResult = new SyncResult();
        SyncTask categoriesSync = new SyncCategoriesCaseImpl(getContext(), syncResult, mockApi);

        categoriesSync.execute(null);
        assertThat((int) syncResult.stats.numInserts, is(items.size()));

        assertItemsAgainstDb(items);
    }

    public void testUpdates() {
        populateDbWithCategories();

        Collection<CategoryItem> items = createCategories(UPDATE_BUCKET_SRC);
        when(mockApi.listPrimaryCategories()).thenReturn(items);

        SyncResult syncResult = new SyncResult();
        SyncTask categoriesSync = new SyncCategoriesCaseImpl(getContext(), syncResult, mockApi);

        categoriesSync.execute(null);
        assertThat((int) syncResult.stats.numUpdates, is(2));

        assertItemsAgainstDb(items);
    }

    public void testDeletes() {
        populateDbWithCategories();

        Collection<CategoryItem> items = createCategories(DELETE_BUCKET_SRC);
        when(mockApi.listPrimaryCategories()).thenReturn(items);

        SyncResult syncResult = new SyncResult();
        SyncTask categoriesSync = new SyncCategoriesCaseImpl(getContext(), syncResult, mockApi);

        categoriesSync.execute(null);
        assertThat((int) syncResult.stats.numDeletes, is(2));

        assertItemsAgainstDb(items);
    }

    private void populateDbWithCategories() {
        Collection<CategoryItem> initialItems = createCategories(INSERT_BUCKET_SRC);
        for (CategoryItem item : initialItems) {
            CategoryContentValues cv = transform(item);
            contentResolver.insert(CONTENT_URI, cv.values());
        }
    }

    private void assertItemsAgainstDb(Collection<CategoryItem> items) {
        for (CategoryItem item : items) {
            Cursor cursor = contentResolver.query(CONTENT_URI, ALL_COLUMNS, SELECT_BY_CATEGORY_ID,
                    new String[] {String.valueOf(item.getId())}, null);
            CategoryContentValues cv = transform(item);
            CursorAssert.validateCursor(cursor, cv.values());
        }
    }

    private CategoryContentValues transform(CategoryItem item) {
        return new CategoryContentValues()
                .putCategoryId((long) item.getId())
                .putName(item.getName())
                .putDescription(item.getDescription());
    }

    private Collection<CategoryItem> createCategories(String src) {
        String categories = TestResource.get(TestResource.DataFormat.JSON).rawData(src);
        Type collectionType = new TypeToken<List<CategoryItem>>() {
        }.getType();
        return new Gson().fromJson(categories, collectionType);
    }
}
