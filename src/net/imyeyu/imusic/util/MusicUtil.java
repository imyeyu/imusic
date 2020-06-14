package net.imyeyu.imusic.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.flac.FlacTagReader;
import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import net.imyeyu.imusic.Entrance;
import net.imyeyu.imusic.bean.Music;
import net.imyeyu.util.ResourceBundleX;
import net.imyeyu.util.YeyuUtils;
import net.imyeyu.util.gui.TipsX;

/**
 * 音频信息加载器
 * 
 * @author Yeyu
 *
 */
public class MusicUtil {
	
	
	private static Music music;

	public static Music getMusicInfo(File musicFile, Label tips) {
		ResourceBundleX rb = Entrance.getRb();
		
		String format = musicFile.getName().substring(musicFile.getName().indexOf(".") + 1);
		
		if (!format.equals("mp3") && !format.equals("flac")) {
			YeyuUtils.gui().tips(tips, rb.def("unsupportedAudioEncoding"), 4000, TipsX.ERROR);
			return null;
		}
		
		format = format.equals("mp3") ? "MPEG1L3" : "FLAC";
		
		music = new Music();
		try {
			music.setTime(new AudioFileIO().readFile(musicFile).getAudioHeader().getTrackLength());
			music.setSize(musicFile.length());
			music.setCover(new Image(Entrance.RES_PATH + "cover.png", -1, -1, true, true));
			music.setEncode(format);
			
			switch (music.getEncode()) {
				case "MPEG1L3":
					getInfoMP3(musicFile);
					music.setCover(getCoverMP3(musicFile));
					break;
				case "FLAC":
					getInfoFLAC(musicFile);
					music.setCover(getCoverFLAC(musicFile));
					break;
			}
		} catch (UnsupportedEncodingException | InvalidAudioFrameException e) {
			YeyuUtils.gui().tips(tips, rb.def("unsupportedAudioEncodingTips") + musicFile.getName(), 4000, TipsX.ERROR);
		} catch (IndexOutOfBoundsException | NullPointerException | ClassCastException e) {
			YeyuUtils.gui().tips(tips, rb.def("coverError"), 4000, TipsX.ERROR);
		} catch (TagException e) {
			YeyuUtils.gui().tips(tips, rb.def("tagError"), 4000, TipsX.ERROR);
		} catch (CannotReadException | ReadOnlyFileException e) {
			YeyuUtils.gui().tips(tips, rb.def("audioError"), 4000, TipsX.ERROR);
		} catch (Exception e) {
			YeyuUtils.gui().tips(tips, rb.def("loaderError"), 4000, TipsX.ERROR);
			e.printStackTrace();
		}
		return music;
	}
	
	private static void getInfoMP3(File musicFile) throws UnsupportedEncodingException, Exception {
		RandomAccessFile raf = new RandomAccessFile(musicFile, "r");
		byte[] buf = new byte[128];
		raf.seek(raf.length() - 128);
		raf.read(buf);
		music.setName(new String(buf, 3, 30, "GBK").trim());
		music.setSinger(new String(buf, 33, 30, "GBK").trim());
		music.setAlbum(new String(buf, 63, 30, "GBK").trim());
		music.setYear(new String(buf, 93 ,4 ,"GBK").trim());
		music.setTitle(music.getName() + " - " + music.getSinger());
		raf.close();
	}
	
	private static void getInfoFLAC(File musicFile) throws CannotReadException, TagException, Exception {
		Tag tag = new FlacFileReader().read(musicFile).getTag();
		music.setName(getFieldFLAC(tag, FieldKey.TITLE));
		music.setSinger(getFieldFLAC(tag, FieldKey.ARTIST));
		music.setAlbum(getFieldFLAC(tag, FieldKey.ALBUM));
		music.setYear(getFieldFLAC(tag, FieldKey.YEAR));
		music.setTitle(music.getName() + " - " + music.getSinger());
	}
	
	private static String getFieldFLAC(Tag tag, FieldKey key) {
		try {
			String value =  tag.getFields(key).toString();
			return value.substring(1, value.length() - 1);
		} catch (NullPointerException e) {
			return "";
		}
	}
	
	private static Image getCoverMP3(File musicFile) throws TagException, NullPointerException, Exception {
		AbstractID3v2Frame frame = (AbstractID3v2Frame) new MP3File(musicFile).getID3v2Tag().getFrame("APIC");
		FrameBodyAPIC body = (FrameBodyAPIC) frame.getBody();
		byte[] imgData = body.getImageData();
		return new Image(new ByteArrayInputStream(imgData));
	}
	
	private static Image getCoverFLAC(File musicFile) throws IndexOutOfBoundsException, CannotReadException, Exception {
		RandomAccessFile raf = new RandomAccessFile(musicFile, "r");
		FlacTag tag = new FlacTagReader().read(raf);
		MetadataBlockDataPicture mbdp = tag.getImages().get(0);
		byte[] imgData = mbdp.getImageData();
		raf.close();
		return new Image(new ByteArrayInputStream(imgData));
	}
}