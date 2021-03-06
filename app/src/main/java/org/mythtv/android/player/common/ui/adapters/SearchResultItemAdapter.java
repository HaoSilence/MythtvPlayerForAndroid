package org.mythtv.android.player.common.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.joda.time.DateTimeZone;
import org.mythtv.android.R;
import org.mythtv.android.library.core.MainApplication;
import org.mythtv.android.library.core.domain.dvr.Program;
import org.mythtv.android.player.common.ui.animation.AnimationUtils;

import java.util.List;

/**
 * Created by dmfrey on 11/29/14.
 */
public class SearchResultItemAdapter extends RecyclerView.Adapter<SearchResultItemAdapter.ViewHolder> {

    private List<Program> programs;
    private ProgramItemClickListener programItemClickListener;
    private int previousPosition = 0;

    public SearchResultItemAdapter( List<Program> programs, @NonNull ProgramItemClickListener programItemClickListener ) {

        this.programs = programs;
        this.programItemClickListener = programItemClickListener;

    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup viewGroup, int position ) {

        View v = LayoutInflater.from( viewGroup.getContext() ).inflate( R.layout.search_result_list_item, viewGroup, false );

        ViewHolder vh = new ViewHolder( v );
        return vh;
    }

    @Override
    public void onBindViewHolder( ViewHolder viewHolder, int position ) {

        final Program program = programs.get( position );

        String title = program.getTitle();
        String subTitle = program.getSubTitle();

        if( !( null == subTitle || "".equals( subTitle ) ) ) {

            viewHolder.setSubTitleVisibility( View.VISIBLE );

        } else {

            viewHolder.setSubTitleVisibility( View.GONE);

        }

        viewHolder.setTitle(title);
        viewHolder.setSubTitle(subTitle);
        viewHolder.setDate( program.getStartTime().withZone( DateTimeZone.getDefault() ).toString( "yyyy-MM-dd hh:mm a" ) );
        viewHolder.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {

                programItemClickListener.onProgramItemClicked( v, program );

            }

        });

//        if( position > previousPosition ) {
//
//            AnimationUtils.animate( viewHolder, true );
//
//        } else {
//
//            AnimationUtils.animate( viewHolder, false );
//
//        }
        previousPosition = position;

        String previewUrl = MainApplication.getInstance().getMasterBackendUrl() + "/Content/GetPreviewImage?ChanId=" + program.getChannel().getChanId() + "&StartTime=" + program.getRecording().getStartTs().withZone( DateTimeZone.UTC ).toString( "yyyy-MM-dd'T'HH:mm:ss" );
        Picasso.with( MainApplication.getInstance() )
            .load( previewUrl )
            .fit().centerCrop()
            .into( viewHolder.preview );


    }

    @Override
    public int getItemCount() {

        return programs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private static String TAG = ViewHolder.class.getSimpleName();

        private final View parent;
        private final ImageView preview;
        private final TextView title;
        private final TextView subTitle;
        private final TextView date;

        public ViewHolder( View v ) {
            super( v );

            this.parent = v;
            preview = (ImageView) parent.findViewById( R.id.program_item_preview );
            title = (TextView) parent.findViewById( R.id.program_item_title );
            subTitle = (TextView) parent.findViewById( R.id.program_item_sub_title );
            date = (TextView) parent.findViewById( R.id.program_item_date );

        }

        public void setTitle( CharSequence text ) {

            title.setText( text );

        }

        public void setSubTitle( CharSequence text ) {

            subTitle.setText(text);

        }

        public void setSubTitleVisibility( int visibility ) {

            subTitle.setVisibility(visibility);

        }

        public void setDate( CharSequence text ) {

            date.setText( text );

        }

        public void setOnClickListener( View.OnClickListener listener ) {

            parent.setOnClickListener( listener );

        }

    }

    public interface ProgramItemClickListener {

        void onProgramItemClicked(View v, Program program);

    }

}
