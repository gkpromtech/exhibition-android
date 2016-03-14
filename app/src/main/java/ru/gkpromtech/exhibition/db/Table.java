package ru.gkpromtech.exhibition.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.InvalidClassException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.TimeZone;

import ru.gkpromtech.exhibition.BuildConfig;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.annotation.Autoincrement;
import ru.gkpromtech.exhibition.model.annotation.Default;
import ru.gkpromtech.exhibition.model.annotation.FK;
import ru.gkpromtech.exhibition.model.annotation.Null;
import ru.gkpromtech.exhibition.model.annotation.PK;
import ru.gkpromtech.exhibition.model.annotation.TableRef;
import ru.gkpromtech.exhibition.model.annotation.Translatable;
import ru.gkpromtech.exhibition.model.annotation.Unique;

public class Table <T extends Entity> {

    private final static int INTEGER = 0;
    private final static int SHORT = 1;
    private final static int LONG = 2;
    private final static int FLOAT = 3;
    private final static int DOUBLE = 4;
    private final static int STRING = 5;
    private final static int BYTE_ARRAY = 6;
    private final static int DATE = 7;
    private final static int BOOLEAN = 8;

    private final String mTableName;
    private final String[] mColumns;
    private final Field[] mFields;
    private final int mType[];
//    private final boolean mTr;
    private final Class<T> mEntityClass;
    private final SQLiteOpenHelper mSqlHelper;
    private final FkInfo[] mFks;
    // 2015-02-27T15:03:47.000Z
    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat mDateFormat =
            new SimpleDateFormat("yyyy-M-d'T'HH:mm:ss.SSS'Z'");
    static {
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static class Join {
        // SELECT FROM table t WHERE ...
        // [<type>] JOIN <entity> e ON e.<entityRow> = t.<row>
        String row;
        Class<? extends Entity> entity;
        String entityRow;
        String type;
        String customJoinOn;

        public Join(String row, Class<? extends Entity> entity, String entityRow, String type) {
            this.row = row;
            this.entity = entity;
            this.entityRow = entityRow;
            this.type = type;
        }

        public Join(String row, Class<? extends Entity> entity, String entityRow) {
            this.row = row;
            this.entity = entity;
            this.entityRow = entityRow;
        }

        public Join(Class<? extends Entity> entity, String customJoinOn) {
            this.entity = entity;
            this.customJoinOn = customJoinOn;
        }

        public Join(Class<? extends Entity> entity, String customJoinOn, String type) {
            this.entity = entity;
            this.customJoinOn = customJoinOn;
            this.type = type;
        }
    }

    private static class FkInfo {
        Class<? extends Entity> entityClass;
        String fkName;
        String fieldName;

        private FkInfo(Class<? extends Entity> entityClass, String fkName, String fieldName) {
            this.entityClass = entityClass;
            this.fkName = fkName;
            this.fieldName = fieldName;
        }
    }

    protected Table(Class<T> entityClass, SQLiteOpenHelper sqlHelper) throws InvalidPropertiesFormatException {

        mEntityClass = entityClass;
        mSqlHelper = sqlHelper;

        TableRef tableRef = entityClass.getAnnotation(TableRef.class);
        mTableName = tableRef.name();
//        mTr = tableRef.tr();

        List<Field> fields = new ArrayList<>();

        for (Field field : mEntityClass.getFields())
            if (!Modifier.isStatic(field.getModifiers()))
                fields.add(field);

        List<FkInfo> fks = new ArrayList<>();
        mFields = fields.toArray(new Field[fields.size()]);
        mColumns = new String[mFields.length];
        mType = new int[mFields.length];
        for (int i = 0; i < mFields.length; ++i) {
            Field field = mFields[i];

            mColumns[i] = field.getName();
            switch (field.getType().getSimpleName()) {
                case "int":
                case "Integer":
                    mType[i] = INTEGER;
                    break;
                case "Short":
                case "short":
                    mType[i] = SHORT;
                    break;
                case "long":
                case "Long":
                    mType[i] = LONG;
                    break;

                case "float":
                case "Float":
                    mType[i] = FLOAT;
                    break;

                case "double":
                case "Double":
                    mType[i] = DOUBLE;
                    break;

                case "String":
                    mType[i] = STRING;
                    break;

                case "byte[]":
                    mType[i] = BYTE_ARRAY;
                    break;

                case "Date":
                    mType[i] = DATE;
                    break;

                case "boolean":
                    mType[i] = BOOLEAN;
                    break;

                default:
                    throw new InvalidPropertiesFormatException("Unsupported type: "
                            + field.getType().getCanonicalName());
            }

            FK fk = field.getAnnotation(FK.class);
            if (fk != null)
                fks.add(new FkInfo(fk.entity(), fk.field(), field.getName()));
        }

        mFks = fks.toArray(new FkInfo[fks.size()]);
    }

    public Class<T> getEntityClass() {
        return mEntityClass;
    }

    public String getCreateQuery() throws InvalidPropertiesFormatException {
        String query = "CREATE TABLE " + mTableName + " (";
        for (int i = 0; i < mFields.length; ++i) {
            Field field = mFields[i];
            if (i != 0)
                query += ",";
            query += "\n  " + field.getName() + " " + getSqlType(mType[i]);
            String clauses = "";
            boolean isNull = false;
            boolean isTr = false;
            boolean isAutoincrement = false;
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotation instanceof Null) {
                    isNull = true;
                } else if (annotation instanceof Translatable) {
                    isTr = true;
                } else if (annotation instanceof FK) {
                    FK refs = (FK)annotation;
                    clauses += " REFERENCES " + refs.entity().getAnnotation(TableRef.class).name()
                            + "(" + refs.field() + ")";
                    if (!refs.onDelete().isEmpty())
                        clauses += " ON DELETE " + refs.onDelete();
                    if (!refs.onUpdate().isEmpty())
                        clauses += " ON UPDATE " + refs.onUpdate();
                } else if (annotation instanceof PK) {
                    clauses += " PRIMARY KEY";
                    if (isAutoincrement)
                        clauses += " AUTOINCREMENT";
                } else if (annotation instanceof Autoincrement) {
                    isAutoincrement = true;
                } else if (annotation instanceof Unique) {
                    clauses += " UNIQUE";
                } else if (annotation instanceof Default) {
                    clauses += " DEFAULT('" + ((Default)annotation).value() + "')";
                } else {
                    throw new InvalidPropertiesFormatException("Unsupported annotation ["
                            + annotation.getClass().getCanonicalName() + "] for entity ["
                            + mEntityClass.getCanonicalName() + "]");
                }
            }
            if (!isNull && !isTr)
                query += " NOT";
            query += " NULL" + clauses;
        }
        query += ");";
        return query;
    }

    public String getSqlType(int type) {
        switch (type) {
            case INTEGER:
            case SHORT:
            case LONG:
            case DATE:
            case BOOLEAN:
                return "INTEGER";
            case FLOAT:
            case DOUBLE:
                return "REAL";
            case STRING:
                return "TEXT";
            case BYTE_ARRAY:
                return "BLOB";
        }
        return null;
    }


    public void onCreate(SQLiteDatabase db) throws InvalidPropertiesFormatException {
        db.execSQL(getCreateQuery());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void fillFieldValue(int type, Field field, Object entity, Cursor cursor, int i)
            throws IllegalAccessException {
        if (cursor.isNull(i)) {
            field.set(entity, null);
            return;
        }
        switch (type) {
            case INTEGER:
                field.set(entity, cursor.getInt(i));
                break;
            case SHORT:
                field.set(entity, cursor.getShort(i));
                break;
            case LONG:
                field.set(entity, cursor.getLong(i));
                break;
            case FLOAT:
                field.set(entity, cursor.getFloat(i));
                break;
            case DOUBLE:
                field.set(entity, cursor.getDouble(i));
                break;
            case STRING:
                field.set(entity, cursor.getString(i));
                break;
            case BYTE_ARRAY:
                field.set(entity, cursor.getBlob(i));
                break;
            case DATE:
                field.set(entity, new Date(cursor.getLong(i)));
                break;
            case BOOLEAN:
                field.set(entity, cursor.getInt(i) != 0);
                break;
        }
    }

    public List<T> select() {
        return select(null, null, null, null, null);
    }

    public List<T> select(String selection, String[] selectionArgs, String groupBy, String having,
                          String orderBy) {
        return select(selection, selectionArgs, groupBy, having, orderBy, null);
    }

    public List<T> select(String selection, String[] selectionArgs, String groupBy, String having,
                          String orderBy, String limit) {
        List<T> result = new ArrayList<>();

        SQLiteDatabase db = mSqlHelper.getReadableDatabase();
        Cursor cursor = db.query(mTableName, mColumns, selection, selectionArgs, groupBy, having,
                orderBy, limit);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            while (cursor.moveToNext()) {
                T entity = mEntityClass.newInstance();
                for (int i = 0; i < mFields.length; ++i)
                    fillFieldValue(mType[i], mFields[i], entity, cursor, i);
                result.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }

        return result;
    }

    // выборка по двум FK из таблицы-связки
    public <F extends Entity, S extends Entity> List<Pair<F, S>> selectLinked(
            Class<F> f, Class<S> s, String selection, String[] selectionArgs, String orderBy)
            throws InvalidClassException, IllegalAccessException, InstantiationException {

        if (mFks.length != 2)
            throw new InvalidClassException("Entity " + mEntityClass.getName() + " is not a link");

        List<Pair<F, S>> result = new ArrayList<>();
        FkInfo fk1;
        FkInfo fk2;

        if (mFks[0].entityClass.equals(f) && mFks[1].entityClass.equals(s)) {
            fk1 = mFks[0];
            fk2 = mFks[1];
        } else if (mFks[1].entityClass.equals(f) && mFks[0].entityClass.equals(s)) {
            // типы могут быть перепутаны местами из за особенностей рефлексии
            fk1 = mFks[1];
            fk2 = mFks[0];
        } else {
            throw new InvalidClassException("Invalid classes passed as arguments");
        }

        Table<F> table1 = ((DbHelper)mSqlHelper).getTableFor(f);
        Table<S> table2 = ((DbHelper)mSqlHelper).getTableFor(s);

        StringBuilder query = new StringBuilder();
        for (String column : table1.mColumns) {
            query.append(",f.").append(column);
        }
        for (String column : table2.mColumns)
            query.append(",s.").append(column);
        query.replace(0, 1, "SELECT ");


        query.append("\nFROM ").append(mTableName).append(" t\nJOIN ").append(table1.mTableName)
                .append(" f ON f.").append(fk1.fkName).append(" = t.").append(fk1.fieldName)
                .append("\nJOIN ").append(table2.mTableName).append(" s ON s.")
                .append(fk2.fkName).append(" = t.").append(fk2.fieldName);
        if (selection != null)
            query.append("\nWHERE ").append(selection);
        if (orderBy != null)
            query.append("\nORDER BY ").append(orderBy);


        String queryString = query.toString();
        if (BuildConfig.DEBUG)
            Log.d("PP", queryString);

        SQLiteDatabase db = mSqlHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, selectionArgs);

        //noinspection TryFinallyCanBeTryWithResources
        try {
            while (cursor.moveToNext()) {
                F entity1 = f.newInstance();
                S entity2 = s.newInstance();
                for (int i = 0; i < table1.mFields.length; ++i)
                    fillFieldValue(table1.mType[i], table1.mFields[i], entity1, cursor, i);
                for (int i = 0; i < table2.mFields.length; ++i)
                    fillFieldValue(table2.mType[i], table2.mFields[i], entity2, cursor,
                            table1.mFields.length + i);
                result.add(new Pair<>(entity1, entity2));
            }
        } finally {
            cursor.close();
            db.close();
        }

        return result;
    }

    public List<Pair<Entity[], T>> selectJoined(
            Join[] joins, String selection, String[] selectionArgs, String orderBy)
            throws InvalidClassException, IllegalAccessException, InstantiationException {
        return selectJoined(joins, selection, selectionArgs, orderBy, null);
    }


        // выборка из таблицы с JOIN других таблиц по произвольным полям
    public List<Pair<Entity[], T>> selectJoined(
            Join[] joins, String selection, String[] selectionArgs, String orderBy, String groupBy)
            throws InvalidClassException, IllegalAccessException, InstantiationException {

        List<Pair<Entity[], T>> result = new ArrayList<>();
        Table<? extends Entity>[] tables = new Table<?>[joins.length];

        StringBuilder query = new StringBuilder();
        for (int i = 0; i < joins.length; ++i) {
            tables[i] = ((DbHelper) mSqlHelper).getTableFor(joins[i].entity);
            for (String column : tables[i].mColumns) {
                query.append(",f").append(i).append(".").append(column);
            }
        }
        for (String column : mColumns)
            query.append(",t.").append(column);
        query.replace(0, 1, "SELECT "); // first comma -> select


        query.append("\nFROM ").append(mTableName).append(" t");
        for (int i = 0; i < joins.length; ++i) {
            Join join = joins[i];
            query.append("\n");
            if (join.type != null)
                query.append(join.type).append(" ");
            query.append("JOIN ").append(tables[i].mTableName).append(" f").append(i)
                    .append(" ON ");
            if (join.customJoinOn != null) {
                query.append(join.customJoinOn);
            } else {
                query.append("f").append(i).append(".")
                        .append(join.entityRow).append(" = t.").append(join.row);
            }
        }

        if (selection != null)
            query.append("\nWHERE ").append(selection);
        if (groupBy != null)
            query.append("\nGROUP BY ").append(groupBy);
        if (orderBy != null)
            query.append("\nORDER BY ").append(orderBy);

        String queryString = query.toString();
        if (BuildConfig.DEBUG)
            Log.d("PP", queryString);

        SQLiteDatabase db = mSqlHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, selectionArgs);

        //noinspection TryFinallyCanBeTryWithResources
        try {
            while (cursor.moveToNext()) {
                int col = 0;
                Entity[] entities = new Entity[joins.length];
                for (int i = 0; i < joins.length; ++i) {
                    Table<? extends Entity> table = tables[i];
                    entities[i] = joins[i].entity.newInstance();
                    for (int j = 0; j < table.mFields.length; ++j, ++col)
                        fillFieldValue(table.mType[j], table.mFields[j], entities[i], cursor, col);
                }

                T entity = mEntityClass.newInstance();
                for (int j = 0; j < mFields.length; ++j, ++col)
                    fillFieldValue(mType[j], mFields[j], entity, cursor, col);
                result.add(new Pair<>(entities, entity));
            }
        } finally {
            cursor.close();
            db.close();
        }

        return result;
    }

    public void insert(T item) {
        insert(item, SQLiteDatabase.CONFLICT_FAIL);
    }

    public void insert(T item, int conflictAlgorithm) {
        SQLiteDatabase db = mSqlHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            db.insertWithOnConflict(mTableName, null, itemToRow(item), conflictAlgorithm);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void insert(List<T> items) {
        insert(items, SQLiteDatabase.CONFLICT_FAIL);
    }

    public void insert(List<T> items, int conflictAlgorithm) {
        SQLiteDatabase db = mSqlHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (T item : items)
                db.insertWithOnConflict(mTableName, null, itemToRow(item), conflictAlgorithm);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void update(T item) {
        update(item, SQLiteDatabase.CONFLICT_FAIL);
    }

    public void update(T item, int conflictAlgorithm) {
        SQLiteDatabase db = mSqlHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            db.updateWithOnConflict(mTableName, itemToRow(item), "id = ?",
                    new String[]{String.valueOf(item.id)}, conflictAlgorithm);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void update(List<T> items) {
        update(items, SQLiteDatabase.CONFLICT_FAIL);
    }

    public void update(List<T> items, int conflictAlgorithm) {
        SQLiteDatabase db = mSqlHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (T item : items)
                db.updateWithOnConflict(mTableName, itemToRow(item), "id = ?",
                        new String[]{String.valueOf(item.id)}, conflictAlgorithm);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private ContentValues itemToRow(T item) throws IllegalAccessException {
        ContentValues values = new ContentValues();

        for (int i = 0; i < mFields.length; ++i) {
            if (mFields[i].get(item) == null) {
                values.putNull(mColumns[i]);
                continue;
            }

            switch (mType[i]) {
                case INTEGER:
                    values.put(mColumns[i], (Integer) mFields[i].get(item));
                    break;
                case SHORT:
                    values.put(mColumns[i], (Short) mFields[i].get(item));
                    break;
                case LONG:
                    values.put(mColumns[i], (Long) mFields[i].get(item));
                    break;
                case FLOAT:
                    values.put(mColumns[i], (Float) mFields[i].get(item));
                    break;
                case DOUBLE:
                    values.put(mColumns[i], (Double) mFields[i].get(item));
                    break;
                case STRING:
                    values.put(mColumns[i], (String) mFields[i].get(item));
                    break;
                case BYTE_ARRAY:
                    values.put(mColumns[i], (byte[]) mFields[i].get(item));
                    break;
                case DATE:
                    values.put(mColumns[i], ((Date) mFields[i].get(item)).getTime());
                    break;
                case BOOLEAN:
                    values.put(mColumns[i], mFields[i].getBoolean(item) ? 1 : 0);
                    break;
            }
        }
        return values;
    }

    private ContentValues jsonToRow(ObjectNode object) throws IllegalAccessException, ParseException {
        ContentValues values = new ContentValues();

        for (int i = 0; i < mFields.length; ++i) {
            String fieldName = mFields[i].getName();
            JsonNode field = object.get(fieldName);
            if (field == null)
                continue;
            if (field.isNull()) {
                values.putNull(mColumns[i]);
                continue;
            }

            switch (mType[i]) {
                case INTEGER:
                    values.put(mColumns[i], field.asInt());
                    break;
                case SHORT:
                    values.put(mColumns[i], (short) field.asInt());
                    break;
                case LONG:
                    values.put(mColumns[i], field.asLong());
                    break;
                case FLOAT:
                    values.put(mColumns[i], (float) field.asDouble());
                    break;
                case DOUBLE:
                    values.put(mColumns[i], field.asDouble());
                    break;
                case STRING:
                    values.put(mColumns[i], field.asText());
                    break;
                case BYTE_ARRAY:
                    values.put(mColumns[i], Base64.decode(field.asText(), Base64.DEFAULT));
                    break;
                case DATE:
                    values.put(mColumns[i], mDateFormat.parse(field.asText()).getTime());
                    break;
                case BOOLEAN:
                    values.put(mColumns[i], field.asBoolean() ? 1 : 0);
                    break;
            }
        }
        return values;
    }

    public void deleteById(List<Integer> ids) {
        if (ids.isEmpty())
            return;

        SQLiteDatabase db = mSqlHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            String args[] = DbHelper.makeArguments(ids.toArray(new Integer[ids.size()]));
            String params = DbHelper.makePlaceholders(args.length);
            db.delete(mTableName, "id IN (" + params + ")", args);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }



    public void insert(SQLiteDatabase db, ObjectNode item, int conflictAlgorithm)
            throws IllegalAccessException, ParseException {
        db.insertWithOnConflict(mTableName, null, jsonToRow(item), conflictAlgorithm);
    }

    public void partialUpdate(SQLiteDatabase db, int id, ObjectNode item, int conflictAlgorithm)
            throws IllegalAccessException, ParseException {
        ContentValues values = jsonToRow(item);
        db.updateWithOnConflict(mTableName, values, "id = ?", new String[] { String.valueOf(id) },
                conflictAlgorithm);
    }

    public void delete(SQLiteDatabase db, int id) {
        db.delete(mTableName, "id = ?", new String[] { String.valueOf(id) });
    }

}
