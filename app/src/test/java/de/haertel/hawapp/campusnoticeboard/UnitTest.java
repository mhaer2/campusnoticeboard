package de.haertel.hawapp.campusnoticeboard;

import android.app.Activity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data.MenuEntry;
import de.haertel.hawapp.campusnoticeboard.impl.navigationMenu.data.NavigationMenuDataHandler;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.Announcement;
import de.haertel.hawapp.campusnoticeboard.impl.noticeBoards.data.DateTypeConverter;
import de.haertel.hawapp.campusnoticeboard.util.AnnouncementTopic;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {

    //AnnouncementTopic
    @Test
    public void initAnnouncementWorks() {
        String testString = "test";
        AnnouncementTopic.initTopic(testString);
        assertEquals(testString, AnnouncementTopic.getTopic());
    }

    //DateTypeConverter
    @Test
    public void convertDateToTimestampNotNull(){
        Long l = DateTypeConverter.dateToTimestamp(new Date());
        assertNotNull(l);
        Date d = DateTypeConverter.fromTimestamp(l);
        assertNotNull(d);
    }

    //AnnounceMent
    @Test
    public void validateDayCount(){
        Announcement announcement = new Announcement("headline", "author", "message", new Date(), "noticeboard");
        String expected = "NEW";
        String result = announcement.getDayCountSincePosted();
        assertEquals(expected, result);
    }

}