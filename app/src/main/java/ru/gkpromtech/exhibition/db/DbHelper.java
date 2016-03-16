/*
 * Copyright 2016 Promtech. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.gkpromtech.exhibition.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Event;
import ru.gkpromtech.exhibition.model.EventFavorite;
import ru.gkpromtech.exhibition.model.EventsOrganization;
import ru.gkpromtech.exhibition.model.EventsPerson;
import ru.gkpromtech.exhibition.model.EventsPlace;
import ru.gkpromtech.exhibition.model.Exhibition;
import ru.gkpromtech.exhibition.model.Group;
import ru.gkpromtech.exhibition.model.GroupsPlace;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.MediaFavorite;
import ru.gkpromtech.exhibition.model.ObjectsMedia;
import ru.gkpromtech.exhibition.model.Online;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.model.Person;
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.model.PlacesOrganization;
import ru.gkpromtech.exhibition.model.Section;
import ru.gkpromtech.exhibition.model.SectionsPlace;
import ru.gkpromtech.exhibition.model.Tag;
import ru.gkpromtech.exhibition.model.TagsObject;
import ru.gkpromtech.exhibition.model.annotation.TableRef;
import ru.gkpromtech.exhibition.utils.Profile;
import ru.gkpromtech.exhibition.utils.SharedData;


public class DbHelper extends SQLiteOpenHelper {

    private static class Change {
        public final static int ADDED = 0;   // запись добавлена
        public final static int UPDATED = 1; // запись обновлена
        public final static int DELETED = 2; // запись удалена

        public int id;
        public String entity;
        public int changetype;
        public int rowid;
        public ObjectNode data;
    }

    private static final int DATABASE_VERSION = 1;

    private static DbHelper mInstance;
    private final Map<Class<? extends Entity>, Table> mTables = new HashMap<>();
    private final Map<String, Class<? extends Entity>> mTableNameToEntity = new HashMap<>();
    private final Set<Class<? extends Entity>> mLocalEntities = new HashSet<>();
    private Context mContext;

    public static DbHelper getInstance(Context context) {
        if (mInstance == null)
            mInstance = new DbHelper(context.getApplicationContext());
        return mInstance;
    }

    public DbHelper(Context context) {
        super(context, SharedData.LOCAL_DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;

        @SuppressWarnings("unchecked")
        final Class<? extends Entity> entities[] = new Class[]{
                Event.class,
                EventsOrganization.class,
                EventsPerson.class,
                EventsPlace.class,
                Exhibition.class,
                Group.class,
                GroupsPlace.class,
                Media.class,
                ru.gkpromtech.exhibition.model.Object.class,
                Organization.class,
                Person.class,
                Place.class,
                PlacesOrganization.class,
                Section.class,
                SectionsPlace.class,
                Tag.class,
                TagsObject.class,
                ObjectsMedia.class,
                EventFavorite.class,
                Online.class,
                MediaFavorite.class
        };

        try {
            for (Class<? extends Entity> entity : entities) {
                Constructor<Table> constructor = Table.class.getDeclaredConstructor(Class.class,
                        SQLiteOpenHelper.class);
                Table table = constructor.newInstance(entity, this);
                mTables.put(entity, table);
            }

            for (Class<? extends Entity> entity : mTables.keySet()) {
                TableRef ref = entity.getAnnotation(TableRef.class);
                if (ref != null) {
                    mTableNameToEntity.put(ref.name(), entity);
                    if (!ref.sync())
                        mLocalEntities.add(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDbPath(Context context, String dbName) {
        return context.getDatabasePath(dbName).getAbsolutePath();
    }

    @SuppressWarnings("unchecked")
    public <T extends Table> T getTableFor(Class<? extends Entity> entity) {
        return (T) mTables.get(entity);
    }

    public Class<? extends Entity> getEntityForTableName(String tableName) {
        return mTableNameToEntity.get(tableName);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // создание пользовательских таблиц
            for (Class<? extends Entity> entity : mLocalEntities)
                getTableFor(entity).onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getDbRevision() {
        return getPrefs().getInt("revision", 0);
    }

    private SharedPreferences getPrefs() {
        return mContext.getSharedPreferences(this.getClass().getName(), Context.MODE_PRIVATE);
    }

    public String getPragma(SQLiteDatabase db, String pragma) {
        String value = null;
        Cursor cursor = db.rawQuery("PRAGMA " + pragma, null);
        if (cursor.moveToNext())
            value = cursor.getString(0);
        cursor.close();
        return value;
    }

    public void applyUpdates(SQLiteDatabase db, JsonNode updates, boolean isStatic) throws Exception {

        JsonNode nodeRev = updates.get("revision");
        if (nodeRev == null)
            return;


        final String synchronous = getPragma(db, "synchronous");
        final String journalMode = getPragma(db, "journal_mode");

        db.rawQuery("PRAGMA synchronous = OFF", null);
        db.rawQuery("PRAGMA journal_mode = MEMORY", null);
        // выключаем FK, иначе обновления могут не установиться из за оптимизации add-update
        db.execSQL("PRAGMA foreign_keys = OFF");

        SharedPreferences prefs = getPrefs();

        int langId = Profile.getInstance(mContext).getLangId();
        int currentRevision = prefs.getInt("revision", 0);
        ObjectMapper mapper = new ObjectMapper();
        int revision = nodeRev.asInt();
        ArrayNode nodeChanges = (ArrayNode) updates.get("changes");

        TypeReference<List<Change>> typeRef = new TypeReference<List<Change>>(){};
        List<Change> changes = mapper.readValue(nodeChanges.traverse(), typeRef);

        Map<Table, List<Integer>> deletedTableRowIds = new HashMap<>();

        try {
            db.beginTransaction();

            for (Change change : changes) {
                if (currentRevision > change.id) {
                    Log.w("PPDB", "Skipping old change #" + change.id);
                    continue;
                }

                boolean tr = change.entity.endsWith("_tr");
                String entityName = !tr ? change.entity
                        : change.entity.substring(0, change.entity.length() - 3);

                Class<? extends Entity> entity = getEntityForTableName(entityName);
                if (entity == null) {
                    Log.e("PPDB", "Cannot find entity for " + entityName);
                    continue;
                }

                Table<? extends Entity> table = getTableFor(entity);
                if (table == null) {
                    Log.e("PPDB", "Cannot find table for entity " + entityName);
                    continue;
                }

                if (!tr) {
                    if (change.data != null) {
                        switch (change.changetype) {
                            case Change.ADDED:
                                table.insert(db, change.data, SQLiteDatabase.CONFLICT_FAIL);
                                break;
                            case Change.UPDATED:
                                change.data.remove("id");
                                table.partialUpdate(db, change.rowid, change.data, SQLiteDatabase.CONFLICT_FAIL);
                                break;
                        }
                    } else {
                        if (change.changetype == Change.DELETED) {
                            List<Integer> ids = deletedTableRowIds.get(table);
                            if (ids == null) {
                                ids = new ArrayList<>();
                                deletedTableRowIds.put(table, ids);
                            }
                            ids.add(change.rowid);
                        }
                    }
                } else if (change.data != null) {
                    int changeLangId = change.data.get("languageid").asInt();
                    if (changeLangId != langId)
                        continue;
                    change.data.remove("languageid");
                    switch (change.changetype) {
                        case Change.ADDED:
                        case Change.UPDATED:
                            TableRef annotation = entity.getAnnotation(TableRef.class);
                            if (annotation == null) {
                                Log.e("PPDB", "Cannot get trid field for entity " + entityName);
                                continue;
                            }
                            String trIdName = annotation.trid();
                            JsonNode nodeTrId = change.data.get(trIdName);
                            if (nodeTrId == null) {
                                Log.e("PPDB", "Change data don't have a field [" + trIdName + "]: "
                                        + entityName);
                                continue;
                            }
                            int id = nodeTrId.asInt();
                            change.data.remove("id");
                            change.data.remove(trIdName);
                            table.partialUpdate(db, id, change.data, SQLiteDatabase.CONFLICT_FAIL);
                            break;
                    }
                }
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        // удаление должно выполняться каскадно, с FK
        db.execSQL("PRAGMA foreign_keys = ON");

        try {
            db.beginTransaction();

            for (Map.Entry<Table, List<Integer>> entry : deletedTableRowIds.entrySet()) {
                Table table = entry.getKey();
                List<Integer> ids = entry.getValue();
                for (Integer id : ids)
                    table.delete(db, id);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (synchronous != null)
            db.rawQuery("PRAGMA synchronous = " + synchronous, null);
        if (journalMode != null)
            db.rawQuery("PRAGMA journal_mode = " + journalMode, null);

        if (revision > currentRevision)
            prefs.edit().putInt("revision", revision).apply();

        if (isStatic)
            prefs.edit().putInt("jsonRevision", revision).apply();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Table table : mTables.values())
            table.onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        db.execSQL("ATTACH DATABASE ? AS " + SharedData.EXHIBITION_DATABASE_NAME,
                new String[]{getDbPath(mContext, SharedData.EXHIBITION_DATABASE_NAME) });
    }


    public static String makePlaceholders(int len) {
        if (len < 1)
            throw new RuntimeException("No placeholders");

        StringBuilder sb = new StringBuilder(len * 2 - 1);
        sb.append("?");
        for (int i = 1; i < len; i++) {
            sb.append(",?");
        }
        return sb.toString();
    }

    public static String[] makeArguments(java.lang.Object[] args) {
        String[] res = new String[args.length];
        for (int i = 0; i < args.length; ++i)
            res[i] = args[i].toString();
        return res;
    }


}
