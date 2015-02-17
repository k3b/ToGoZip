package de.k3b.io;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by k3b on 17.02.2015.
 */
public class FileNameUtilTests {
    @Test
    public void shouldGenerateWithDefaultExtension() {
        // "content://com.mediatek.calendarimporter/1282"
        String result = FileNameUtil.createFileName("1282", "vcs");
        Assert.assertEquals("1282.vcs", result);
    }

    @Test
    public void shouldRemoveIllegalWithExistingExtension() {
        String result = FileNameUtil.createFileName("...hello:world.jpeg...", "jpg");
        Assert.assertEquals("hello_world.jpeg", result);
    }

}