package org.mythtv.android.library.persistence.service.dvr;

import org.mythtv.android.library.events.dvr.ChannelInfoDetails;
import org.mythtv.android.library.events.dvr.ProgramDetails;
import org.mythtv.android.library.persistence.domain.dvr.ChannelInfo;
import org.mythtv.android.library.persistence.domain.dvr.Program;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmfrey on 11/15/14.
 */
public class ChannelInfoHelper {

    public static ChannelInfoDetails toDetails( ChannelInfo channelInfo ) {

        ChannelInfoDetails details = new ChannelInfoDetails();
        details.setChanId( channelInfo.getChanId() );
        details.setChanNum( channelInfo.getChanNum() );
        details.setCallSign( channelInfo.getCallSign() );
        details.setIconURL( channelInfo.getIconURL() );
        details.setChannelName( channelInfo.getChannelName() );
        details.setMplexId( channelInfo.getMplexId() );
        details.setServiceId( channelInfo.getServiceId() );
        details.setaTSCMajorChan( channelInfo.getaTSCMajorChan() );
        details.setaTSCMinorChan( channelInfo.getaTSCMinorChan() );
        details.setFormat( channelInfo.getFormat() );
        details.setFrequencyId( channelInfo.getFrequencyId() );
        details.setFineTune( channelInfo.getFineTune() );
        details.setChanFilters( channelInfo.getChanFilters() );
        details.setSourceId( channelInfo.getSourceId() );
        details.setInputId( channelInfo.getInputId() );
        details.setCommFree( channelInfo.getCommFree() );
        details.setUseEIT( channelInfo.getUseEIT() );
        details.setVisible( channelInfo.getVisible() );
        details.setxMLTVID( channelInfo.getxMLTVID() );
        details.setDefaultAuth( channelInfo.getDefaultAuth() );

        List<ProgramDetails> programDetails = new ArrayList<ProgramDetails>();
        if( null != channelInfo.getPrograms() &&  channelInfo.getPrograms().size() > 0 ) {
            for( Program program : channelInfo.getPrograms() ) {
                programDetails.add( ProgramHelper.toDetails(program) );
            }
        }
        details.setPrograms( programDetails );

        return details;
    }

    public static ChannelInfo fromDetails( ChannelInfoDetails details ) {

        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setChanId( details.getChanId() );
        channelInfo.setChanNum( details.getChanNum() );
        channelInfo.setCallSign( details.getCallSign() );
        channelInfo.setIconURL( details.getIconURL() );
        channelInfo.setChannelName( details.getChannelName() );
        channelInfo.setMplexId( details.getMplexId() );
        channelInfo.setServiceId( details.getServiceId() );
        channelInfo.setaTSCMajorChan(details.getaTSCMajorChan());
        channelInfo.setaTSCMinorChan(details.getaTSCMinorChan());
        channelInfo.setFormat( details.getFormat() );
        channelInfo.setFrequencyId( details.getFrequencyId() );
        channelInfo.setFineTune( details.getFineTune() );
        channelInfo.setChanFilters( details.getChanFilters() );
        channelInfo.setSourceId( details.getSourceId() );
        channelInfo.setInputId( details.getInputId() );
        channelInfo.setCommFree( details.getCommFree() );
        channelInfo.setUseEIT( details.getUseEIT() );
        channelInfo.setVisible( details.getVisible() );
        channelInfo.setxMLTVID(details.getxMLTVID());
        channelInfo.setDefaultAuth( details.getDefaultAuth() );

        List<Program> programs = new ArrayList<Program>();
        if( null != details.getPrograms() && !details.getPrograms().isEmpty() ) {
            for( ProgramDetails detail : details.getPrograms() ) {
                programs.add( ProgramHelper.fromDetails(detail) );
            }
        }
        channelInfo.setPrograms( programs );

        return channelInfo;
    }

}
