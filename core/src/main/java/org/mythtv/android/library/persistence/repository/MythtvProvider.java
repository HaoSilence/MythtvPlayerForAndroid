package org.mythtv.android.library.persistence.repository;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.mythtv.android.library.persistence.domain.content.LiveStreamConstants;

import java.util.ArrayList;

/**
 * Created by dmfrey on 1/25/15.
 */
public class MythtvProvider extends ContentProvider {

    private static final String TAG = MythtvProvider.class.getSimpleName();

    public static final String AUTHORITY = "org.mythtv.android.provider";

    private static final UriMatcher URI_MATCHER;

    private DatabaseHelper mOpenHelper;
    private SQLiteDatabase db;

    static {

        URI_MATCHER = new UriMatcher( UriMatcher.NO_MATCH );

        URI_MATCHER.addURI( AUTHORITY, LiveStreamConstants.TABLE_NAME, LiveStreamConstants.ALL );
        URI_MATCHER.addURI( AUTHORITY, LiveStreamConstants.TABLE_NAME + "/#",  LiveStreamConstants.SINGLE );
        URI_MATCHER.addURI( AUTHORITY, LiveStreamConstants.TABLE_NAME + "/recordings", LiveStreamConstants.ALL_RECORDINGS );
        URI_MATCHER.addURI( AUTHORITY, LiveStreamConstants.TABLE_NAME + "/recordings/#",  LiveStreamConstants.SINGLE_RECORDING );
        URI_MATCHER.addURI( AUTHORITY, LiveStreamConstants.TABLE_NAME + "/videos", LiveStreamConstants.ALL_VIDEOS );
        URI_MATCHER.addURI( AUTHORITY, LiveStreamConstants.TABLE_NAME + "/videos/#",  LiveStreamConstants.SINGLE_VIDEO );

    }

    @Override
    public boolean onCreate() {
        Log.i( TAG, "onCreate : enter" );

        mOpenHelper = new DatabaseHelper( getContext() );

        Log.i( TAG, "onCreate : exit" );
        return true;
    }

    @Override
    public String getType( Uri uri ) {
//        Log.v( TAG, "getType : enter" );

        switch( URI_MATCHER.match( uri ) ) {

            case LiveStreamConstants.ALL :
            case LiveStreamConstants.ALL_RECORDINGS :
            case LiveStreamConstants.ALL_VIDEOS :
                return LiveStreamConstants.CONTENT_TYPE;

            case LiveStreamConstants.SINGLE :
            case LiveStreamConstants.SINGLE_RECORDING :
            case LiveStreamConstants.SINGLE_VIDEO :
                return LiveStreamConstants.CONTENT_ITEM_TYPE;

            default :
                throw new IllegalArgumentException( "Unknown URI : " + uri );

        }

    }

    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder ) {
//        Log.v( TAG, "query : enter" );

        db = mOpenHelper.getReadableDatabase();

        Cursor cursor = null;

        switch( URI_MATCHER.match( uri ) ) {

            case LiveStreamConstants.ALL :
//                Log.v( TAG, "query : querying for all live streams" );

                cursor = db.query( LiveStreamConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case LiveStreamConstants.ALL_RECORDINGS :
//                Log.v( TAG, "query : querying for all recordings" );

                selection = LiveStreamConstants.FIELD_RECORDED_ID + " IS NOT NULL OR (" + LiveStreamConstants.FIELD_CHAN_ID + " IS NOT NULL AND " + LiveStreamConstants.FIELD_START_TIME + " IS NOT NULL)" + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" );

                cursor = db.query( LiveStreamConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case LiveStreamConstants.ALL_VIDEOS :
//                Log.v( TAG, "query : querying for all videos" );

                selection = LiveStreamConstants.FIELD_VIDEO_ID + " IS NOT NULL" + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" );

                cursor = db.query( LiveStreamConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case LiveStreamConstants.SINGLE :
//                Log.v( TAG, "query : querying for single live stream" );

                selection = LiveStreamConstants._ID + " = " + uri.getLastPathSegment() + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" );

                cursor = db.query( LiveStreamConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case LiveStreamConstants.SINGLE_RECORDING :
//                Log.v( TAG, "query : querying for single recording" );

                selection = LiveStreamConstants.FIELD_RECORDED_ID + " = " + uri.getLastPathSegment() + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" );

                cursor = db.query( LiveStreamConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            case LiveStreamConstants.SINGLE_VIDEO :
//                Log.v( TAG, "query : querying for single video" );

                selection = LiveStreamConstants.FIELD_VIDEO_ID + " = " + uri.getLastPathSegment() + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" );

                cursor = db.query( LiveStreamConstants.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );

                cursor.setNotificationUri( getContext().getContentResolver(), uri );

                return cursor;

            default :
                throw new IllegalArgumentException( "Unknown URI : " + uri );

        }

    }

    @Override
    public Uri insert( Uri uri, ContentValues values ) {
//        Log.v( TAG, "insert : enter" );

        db = mOpenHelper.getWritableDatabase();

        Uri newUri = null;

        switch( URI_MATCHER.match( uri ) ) {

            case LiveStreamConstants.ALL:
//                Log.v( TAG, "insert : inserting new live stream" );

                newUri = ContentUris.withAppendedId( LiveStreamConstants.CONTENT_URI, db.insertWithOnConflict( LiveStreamConstants.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE ) );

                getContext().getContentResolver().notifyChange( newUri, null );

                return newUri;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );

        }

    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs ) {
//        Log.v( TAG, "delete : enter" );

        db = mOpenHelper.getWritableDatabase();

        int deleted = 0;

        switch( URI_MATCHER.match( uri ) ) {

            case LiveStreamConstants.ALL:

                deleted = db.delete( LiveStreamConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case LiveStreamConstants.SINGLE:

                deleted = db.delete( LiveStreamConstants.TABLE_NAME, LiveStreamConstants._ID
                        + "="
                        + uri.getLastPathSegment()
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case LiveStreamConstants.ALL_RECORDINGS:

                deleted = db.delete( LiveStreamConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case LiveStreamConstants.SINGLE_RECORDING:

                selection = LiveStreamConstants.FIELD_RECORDED_ID + " IS NOT NULL OR (" + LiveStreamConstants.FIELD_CHAN_ID + " IS NOT NULL AND " + LiveStreamConstants.FIELD_START_TIME + " IS NOT NULL)" + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" );

                deleted = db.delete( LiveStreamConstants.TABLE_NAME, LiveStreamConstants.FIELD_RECORDED_ID
                        + "="
                        + uri.getLastPathSegment()
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case LiveStreamConstants.ALL_VIDEOS:

                selection = LiveStreamConstants.FIELD_VIDEO_ID + " IS NOT NULL" + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" );

                deleted = db.delete( LiveStreamConstants.TABLE_NAME, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            case LiveStreamConstants.SINGLE_VIDEO:

                deleted = db.delete( LiveStreamConstants.TABLE_NAME, LiveStreamConstants.FIELD_VIDEO_ID
                        + "="
                        + uri.getLastPathSegment()
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" )
                        , selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return deleted;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );

        }

    }

    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs ) {
//        Log.v( TAG, "update : enter" );

        db = mOpenHelper.getWritableDatabase();

        int affected = 0;

        switch( URI_MATCHER.match( uri ) ) {

            case LiveStreamConstants.ALL:

                affected = db.update( LiveStreamConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case LiveStreamConstants.SINGLE:

                affected = db.update( LiveStreamConstants.TABLE_NAME, values, LiveStreamConstants._ID
                        + "="
                        + uri.getLastPathSegment()
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" ),
                        selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case LiveStreamConstants.ALL_RECORDINGS:

                selection = LiveStreamConstants.FIELD_RECORDED_ID + " IS NOT NULL OR (" + LiveStreamConstants.FIELD_CHAN_ID + " IS NOT NULL AND " + LiveStreamConstants.FIELD_START_TIME + " IS NOT NULL)" + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" );

                affected = db.update( LiveStreamConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case LiveStreamConstants.SINGLE_RECORDING:

                affected = db.update( LiveStreamConstants.TABLE_NAME, values, LiveStreamConstants.FIELD_RECORDED_ID
                                + "="
                                + uri.getLastPathSegment()
                                + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" ),
                        selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case LiveStreamConstants.ALL_VIDEOS:

                selection = LiveStreamConstants.FIELD_VIDEO_ID + " IS NOT NULL" + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" );

                affected = db.update( LiveStreamConstants.TABLE_NAME, values, selection, selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            case LiveStreamConstants.SINGLE_VIDEO:

                affected = db.update( LiveStreamConstants.TABLE_NAME, values, LiveStreamConstants.FIELD_VIDEO_ID
                        + "="
                        + uri.getLastPathSegment()
                        + ( !TextUtils.isEmpty( selection ) ? " AND (" + selection + ")" : "" ),
                        selectionArgs );

                getContext().getContentResolver().notifyChange( uri, null );

                return affected;

            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );

        }

    }

    @Override
    public ContentProviderResult[] applyBatch( ArrayList<ContentProviderOperation> operations )	 throws OperationApplicationException {
//        Log.v( TAG, "applyBatch : enter" );

        db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {

            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[ numOperations ];
            for( int i = 0; i < numOperations; i++ ) {
                results[ i ] = operations.get( i ).apply( this, results, i );
            }
            db.setTransactionSuccessful();

//            Log.v( TAG, "applyBatch : exit" );
            return results;

        } finally {
            db.endTransaction();
        }

    }

}
