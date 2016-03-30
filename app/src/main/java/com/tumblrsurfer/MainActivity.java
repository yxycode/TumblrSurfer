package com.tumblrsurfer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.ProgressDialog;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.graphics.drawable.BitmapDrawable;
import android.view.View.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
//import android.webkit.CookieManager;
import java.net.CookieManager;
import java.net.CookieHandler;
import java.io.ByteArrayOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.webkit.WebView;

import java.lang.Math;
import java.util.ArrayList;
import java.net.URLEncoder;

import android.app.AlertDialog;
import android.content.DialogInterface;

import java.util.regex.Pattern;
import java.util.Stack;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.view.View.OnGenericMotionListener;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Scanner;

//--------------------------------------------------------------------------------------------------

public class MainActivity extends ActionBarActivity {

    //--------------------------------------------------------------------------------------------------
//##################################################################################################
//--------------------------------------------------------------------------------------------------
    protected class Internet
    {
        public final static int HTTP_GET = 1;
        public final static int HTTP_POST = 2;

        public final static int STATUS_EMPTY = 0;
        public final static int STATUS_LOADING = 1;
        public final static int STATUS_DONE = 2;
        public final static int STATUS_NULL_STREAM = 3;
        public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";

        private ProgressDialog ProgressDialogObj;

        public final static int MAX_INPUTSTREAM_COUNT = 100;

        public byte[][] ByteArrayList = new byte[MAX_INPUTSTREAM_COUNT][];
        public int ByteArrayListStatus[] = new int[MAX_INPUTSTREAM_COUNT];
        public String[][] HeaderList = new String[MAX_INPUTSTREAM_COUNT][];
        protected short ByteArrayListIndex = 0;

        public Context ItsContext;
        public int ResponseCode = 0;
        protected CookieManager ItsCookieManager;
        protected String ItsCookie = "";

        //--------------------------------------------------------------------------------------------------
        public Internet(Context itscontext )
        {
            ItsCookieManager = new CookieManager();
            CookieHandler.setDefault(ItsCookieManager);

            int i;
            for( i = 0; i < MAX_INPUTSTREAM_COUNT; i++ )
            {
                ByteArrayListStatus[i] = STATUS_EMPTY;
                ByteArrayList[i] = new byte[1];
                HeaderList[i] = new String[1];
            }

            ItsContext = itscontext;

        }

        //--------------------------------------------------------------------------------------------------
        private void SaveCookie( String header )
        {
            String[] headercopy = { "" };
            String tempstr = "";

            headercopy[0] = new String(header);
            tempstr = StrCut( "Set-Cookie, [", "]", headercopy );

            if( tempstr != null )
                if( tempstr.length() >= 3 )
                    ItsCookie = tempstr;
        }
        //--------------------------------------------------------------------------------------------------
        private String OpenHttp(String urlstr, int connecttype, String arguments, String[] headerout ) throws Exception
        {

            if( connecttype == HTTP_GET )
            {
                URL obj = new URL(urlstr);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                con.setRequestProperty("User-Agent", USER_AGENT);
                con.setRequestProperty("Cookie", ItsCookie);
                con.setFollowRedirects(true);

                int responseCode = con.getResponseCode();

                BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();

                if (responseCode == HttpURLConnection.HTTP_OK)
                    headerout[0] = GetHeader(con);

                SaveCookie(headerout[0]);
                con.disconnect();
                return response.toString();
            }
            else
            if( connecttype == HTTP_POST )
            {
                URL obj = new URL(urlstr);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                //add reuqest header
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", USER_AGENT);
                con.setRequestProperty("Cookie", ItsCookie);
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("Content-Length", String.valueOf(arguments.length()));
                con.setFollowRedirects(true);

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(arguments);
                wr.flush();


                int responseCode = con.getResponseCode();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);

                in.close();
                wr.close();

                //if (responseCode == HttpURLConnection.HTTP_OK)
                headerout[0] = "ResponseCode, [" + responseCode + "]; " + GetHeader(con) + "; " + response.toString();

                SaveCookie(headerout[0]);
                con.disconnect();
                return response.toString();
            }
            return null;
        }
        //--------------------------------------------------------------------------------------------------
        protected String GetHeader( HttpURLConnection httpc )
        {
            Map<String, List<String>> hdrs = httpc.getHeaderFields();
            Set<String> hdrKeys = hdrs.keySet();
            String outstr = "";

            for (String k : hdrKeys)
                outstr = outstr + k + ", " + hdrs.get(k) + ";";
            return outstr;
        }
        //--------------------------------------------------------------------------------------------------
        protected void GotoUrl(final String urlstr, final int connecttype, final String arguments, final short arrayindex )
        {

            //ProgressDialogObj = ProgressDialog.show( ItsContext, "", "Downloading..." );
            final String url = urlstr;
            final String[] headerout = { "" };
            final String[] outputstr = { "" };

            new Thread() {
                public void run() {


                    Message msg = Message.obtain();
                    msg.what = 1;

                    try {
                        outputstr[0] = OpenHttp(url, connecttype, arguments, headerout );

                        if( outputstr[0] == null )
                        {
                            ByteArrayListStatus[arrayindex] = STATUS_NULL_STREAM;
                            HeaderList[arrayindex][0] = new String(headerout[0]);
                            return;
                        }

                        ByteArrayListStatus[arrayindex] = STATUS_LOADING;
                        ByteArrayList[arrayindex] = outputstr[0].getBytes(Charset.forName("UTF-8"));
                        HeaderList[arrayindex][0] = new String(headerout[0]);
                        Bundle b = new Bundle();
                        ByteArrayListIndex = arrayindex;
/*
               b.putShort("arrayindex", (short)arrayindex );
               msg.setData(b);
*/
                    }

                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    catch (Exception e) {

                    }
                    messageHandler.sendMessage(msg);
                }
            }.start();
        }
        //--------------------------------------------------------------------------------------------------
        private Handler messageHandler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                short arrayindex = ByteArrayListIndex; //msg.getData().getShort("arrayindex");
                ByteArrayListStatus[arrayindex] = STATUS_DONE;
                //ProgressDialogObj.dismiss();
            }
        };
        //--------------------------------------------------------------------------------------------------
        protected byte[] InputStream2ByteArray(InputStream input) throws IOException
        {
            byte[] buffer = new byte[1000];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((bytesRead = input.read(buffer)) != -1)
            {
                output.write(buffer, 0, bytesRead);
            }
            return output.toByteArray();
        }
        //--------------------------------------------------------------------------------------------------
        public void ClearByteArrayListDoneStatus()
        {
            int i;
            for( i = 0; i < MAX_INPUTSTREAM_COUNT; i++ )
                if( ByteArrayListStatus[i] == STATUS_DONE )
                    ByteArrayListStatus[i] = STATUS_EMPTY;
        }
        //--------------------------------------------------------------------------------------------------
        public String Decode( String inputstr )
        {
            int i;

            for( i = 0; i < 2; i++ )
            {
                inputstr = inputstr.replace( "\\\\\\/", "/" );
                inputstr = inputstr.replace( "\\\\/", "/" );
                inputstr = inputstr.replace( "\\/", "/" );
                inputstr = inputstr.replace( "\\n", "\n" );
                inputstr = inputstr.replace( "\\&", "&" );
                inputstr = inputstr.replace( "\\\"", "\"" );
                inputstr = inputstr.replace( "&nbsp;", " " );
                inputstr = inputstr.replace( "&lt;", "<" );
                inputstr = inputstr.replace( "&gt;", ">" );
                inputstr = inputstr.replace( "&quot;", "\"" );
                inputstr = inputstr.replace( "&amp;", "&" );
                inputstr = inputstr.replace( "\\u0022", "\"" );
                inputstr = inputstr.replace( "\\u0026", "&" );
                inputstr = inputstr.replace( "\\u0027", "'" );
                inputstr = inputstr.replace( "\\u003C", "<" );
                inputstr = inputstr.replace( "\\u003E", ">" );
                inputstr = inputstr.replace("\\u2019", "'" );
            }
            //inputstr = inputstr.replace( "", "" );

            return inputstr;
        }
//--------------------------------------------------------------------------------------------------
    }
    //--------------------------------------------------------------------------------------------------
//##################################################################################################
//--------------------------------------------------------------------------------------------------
    class CustomStack<A>
    {
        //--------------------------------------------------------------------------------------------------
        class CustomStackItem<A>
        {
            public CustomStackItem<A> Previous = null;
            public A Value = null;
            public CustomStackItem<A> Next = null;
        }
//--------------------------------------------------------------------------------------------------

        CustomStackItem<A> HeadItem = null;

        //--------------------------------------------------------------------------------------------------
        public void PushFirst( A obj )
        {
            CustomStackItem<A> csi = new CustomStackItem<A>();

            if( HeadItem == null )
            {
                csi.Value = obj;
                HeadItem = csi;
            }
            else
            {
                csi.Value = obj;
                csi.Next = HeadItem;
                HeadItem.Previous = csi;
                HeadItem = csi;
            }
        }
        //--------------------------------------------------------------------------------------------------
        public void PushLast( A obj )
        {
            CustomStackItem<A> csi = new CustomStackItem<A>();
            CustomStackItem<A> TailItem = PeekLastItem();

            if( TailItem == null )
            {
                PushFirst(obj);
            }
            else
            {
                csi.Value = obj;
                csi.Previous = TailItem;
                TailItem.Next = csi;
            }
        }
        //--------------------------------------------------------------------------------------------------
        public A PopFirst()
        {
            CustomStackItem<A> csi;
            A returnvalue = null;

            if( HeadItem != null )
            {
                returnvalue = HeadItem.Value;

                csi = HeadItem.Next;
                if( csi != null )
                {
                    csi.Previous = null;
                    HeadItem = csi;
                }
                else
                    HeadItem = null;
            }
            return returnvalue;
        }
        //--------------------------------------------------------------------------------------------------
        public A PopLast()
        {
            CustomStackItem<A> csi, tailitem;
            A returnvalue = null;

            tailitem = PeekLastItem();

            if( tailitem != null )
            {
                returnvalue = tailitem.Value;
                csi = tailitem.Previous;

                if( csi != null )
                    csi.Next = null;
                else
                    HeadItem = null;
            }

            return returnvalue;
        }
        //--------------------------------------------------------------------------------------------------
        public A PeekFirst()
        {
            CustomStackItem<A> csi;
            A returnvalue = null;

            if( HeadItem != null )
                returnvalue = HeadItem.Value;

            return returnvalue;
        }
        //--------------------------------------------------------------------------------------------------
        public A PeekLast()
        {
            CustomStackItem<A> csi;
            A returnvalue = null;

            csi = PeekLastItem();

            if( csi != null )
                returnvalue = csi.Value;

            return returnvalue;
        }
        //--------------------------------------------------------------------------------------------------
        private CustomStackItem<A> PeekLastItem()
        {
            CustomStackItem<A> csi;

            csi = HeadItem;

            do
            {
                if( csi == null || csi.Next == null )
                    break;
                csi = csi.Next;

            } while(true);

            return csi;
        }
        //--------------------------------------------------------------------------------------------------
        public void Clear()
        {
            HeadItem = null;
        }
//--------------------------------------------------------------------------------------------------
    }
    //--------------------------------------------------------------------------------------------------
//##################################################################################################
//--------------------------------------------------------------------------------------------------
    class Tumblr
    {
        public String api_key = "AjL8Cs5M0BEvbAu8Xc8KlaYWu03BNmLYd3qCxVJFi2GNC6PGQE";
        public String consumer_key = api_key;
        public String consumer_secret = "ouKrGUhBKc5bEXgKeGEDMhU6JcJWBDUc1WKZQkAOEWF83ljcLZ";

        protected MainActivity MainActivityObj;
        protected Internet InternetObj;
        public String outputstr = "";

        public final int RESET_SEARCH = 0;
        public final int PHOTO_EXPLORE = 1;
        public final int PHOTO_SEARCH = 2;
        public final int AUTHOR_SEARCH = 3;

        protected int SearchType = PHOTO_EXPLORE;
        protected String KeyWordStr = "";
        protected CustomStack<String> UrlStack;
        protected CustomStack<String> ResponseStack;
        protected CustomStack<String> blogidstack;
        protected CustomStack<String> postidstack;
        protected CustomStack<String> WebViewDataStack;
        protected int ImagesFound = 0;
        protected int OAuthStep = 0;

        private ProgressDialog ProgressDialogObj = null;
        private String form_key = "";

        //--------------------------------------------------------------------------------------------------
        public Tumblr( MainActivity mainactivityobj , Internet internetobj )
        {
            MainActivityObj = mainactivityobj;
            InternetObj = internetobj;

            blogidstack = new CustomStack<String>();
            postidstack = new CustomStack<String>();
            UrlStack = new CustomStack<String>();
            ResponseStack = new CustomStack<String>();
            WebViewDataStack = new CustomStack<String>();
        }
        //--------------------------------------------------------------------------------------------------
        public int StrCountChar(String mainstr, String substr)
        {
            if ( mainstr.length() < 1 || substr.length() < 1 )
                return 0;

            int count = 0;
            int index = 0;
            while ((index = mainstr.indexOf(substr, index)) != -1)
            {
                count++;
                index += substr.length();
            }
            return count;
        }
        //--------------------------------------------------------------------------------------------------
        public String urlencode( String str )
        {
            try
            {
                str = URLEncoder.encode(str, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
            return str;
        }
        //--------------------------------------------------------------------------------------------------
        public boolean CheckWorking()
        {
            if( UrlStack.PeekLast() != null || ResponseStack.PeekLast() != null )
                return true;
            else
                return false;
        }
        //--------------------------------------------------------------------------------------------------
        public void Search( String keywordstr, int searchtype )
        {

            SearchType = searchtype;
            KeyWordStr = keywordstr;
            String[] headerout = { "" };

            String url = "", PeekLastStr = UrlStack.PeekLast();
            ItsTimerTask.SetSpeed(1);

            if( searchtype == RESET_SEARCH )
            {
                ClearStacks();
                return;
            }

            if( KeyWordStr == null )
                SearchType = searchtype = PHOTO_EXPLORE;
            else
            if( KeyWordStr.length() < 1 )
                SearchType = searchtype = PHOTO_EXPLORE;

            if( searchtype == PHOTO_SEARCH )
            {
                if( PeekLastStr == null )
                {
                    ProgressDialogObj = ProgressDialog.show( MainActivityObj, "", "Downloading..." );
                    url = "https://www.tumblr.com/search/" + urlencode(KeyWordStr);

                    InternetObj.GotoUrl( url , Internet.HTTP_GET, "", (short)0 );
                    UrlStack.PushLast( url );
                    ImagesFound = 0;
                }
                else
                if( PeekLastStr.indexOf( "https://www.tumblr.com/search/" ) >= 0 )
                {
                    UrlStack.PopLast();

                    ExtractPhotoPostsSearch( ResponseStack.PopLast(), blogidstack, postidstack );

                    UrlStack.PushLast("***FINISHED***");

                    for(;;)
                    {
                        if( blogidstack.PeekLast() == null || postidstack.PeekLast() == null )
                            break;

                        url = CreateTumblrApiUrl( blogidstack.PopLast(), postidstack.PopLast(), "photo" );
                        UrlStack.PushLast( url );
                    }

                    url = "https://api.tumblr.com/v2/tagged?tag=" + urlencode(KeyWordStr) + "&api_key=" + api_key;
                    UrlStack.PushLast(url);

                    InternetObj.GotoUrl( url , Internet.HTTP_GET, "", (short)0 );
                    UrlStack.PopLast();
                }
                else
                if( PeekLastStr.indexOf( "https://api.tumblr.com" ) >= 0 )
                {
                    ProgressDialogObj.dismiss();
                    ItsTimerTask.SetSpeed(10);
                    String buffer[] = { ResponseStack.PopLast() };

                    url = UrlStack.PeekLast();
                    InternetObj.GotoUrl( url , Internet.HTTP_GET, "", (short)0 );
                    UrlStack.PopLast();

                    String[] subbuffer = { "" };

                    subbuffer[0] = ExtractBraceGroupJson( buffer, "{\"blog_name\":\"", '{', '}' );

                    if( subbuffer[0] != null )
                        if( subbuffer[0].length() > 10 )
                        {
                            MainActivityObj.AddTumblrWebView( CreatePost( subbuffer ));
                            ImagesFound++;
                        }

                    do
                    {
                        subbuffer[0] = ExtractBraceGroupJson( buffer, "{\"blog_name\":\"", '{', '}' );

                        if( subbuffer[0] == null )
                            break;
                        else
                        if( subbuffer[0].length() <= 1 )
                            break;

                        WebViewDataStack.PushLast( subbuffer[0] );
                        ImagesFound++;

                    }while(true);

                    if( ImagesFound <= 0 )
                        AddErrorPage( );
                }
                else
                if( PeekLastStr.indexOf( "***FINISHED***" ) >= 0 )
                {
                    //PopAddTumblrWebView(5);
                    ClearStacks();
                }
            }
            else
            if( searchtype == PHOTO_EXPLORE )
            {
                if( PeekLastStr == null )
                {
                    ProgressDialogObj = ProgressDialog.show( MainActivityObj, "", "Downloading..." );
                    url = "https://www.tumblr.com/explore/photos";

                    InternetObj.GotoUrl(url , Internet.HTTP_GET, "", (short)0 );
                    UrlStack.PushLast( url );
                }
                else
                if( PeekLastStr.indexOf("https://www.tumblr.com/explore/photos" ) >= 0 )
                {
                    UrlStack.PopLast();
                    ExtractPhotoPostsSearch( ResponseStack.PopLast(), blogidstack, postidstack );

                    url = "https://www.tumblr.com/explore/gifs";
                    InternetObj.GotoUrl( url, Internet.HTTP_GET, "", (short)0 );
                    UrlStack.PushLast( url );
                }
                else
                if( PeekLastStr.indexOf("https://www.tumblr.com/explore/gifs" ) >= 0 )
                {
                    UrlStack.PopLast();
                    ExtractPhotoPostsSearch( ResponseStack.PopLast(), blogidstack, postidstack );

                    UrlStack.PushLast("***FINISHED***");

                    for(;;)
                    {
                        if( blogidstack.PeekLast() == null || postidstack.PeekLast() == null )
                            break;

                        url = CreateTumblrApiUrl( blogidstack.PopLast(), postidstack.PopLast(), "photo" );
                        UrlStack.PushLast( url );
                    }

                    InternetObj.GotoUrl( url , Internet.HTTP_GET, "", (short)0 );
                    UrlStack.PopLast();
                }
                else
                if( PeekLastStr.indexOf( "https://api.tumblr.com" ) >= 0 )
                {
                    ProgressDialogObj.dismiss();
                    ItsTimerTask.SetSpeed(10);

                    String buffer[] = { ResponseStack.PopLast() };

                    url = UrlStack.PeekLast();
                    InternetObj.GotoUrl( url , Internet.HTTP_GET, "", (short)0 );
                    UrlStack.PopLast();

                    String[] subbuffer = { "" };

                    subbuffer[0] = ExtractBraceGroupJson( buffer, "{\"blog_name\":\"", '{', '}' );

                    if( subbuffer[0] != null )
                        if( subbuffer[0].length() > 10 )
                            MainActivityObj.AddTumblrWebView( CreatePost( subbuffer ));

                    do
                    {
                        subbuffer[0] = ExtractBraceGroupJson( buffer, "{\"blog_name\":\"", '{', '}' );

                        if( subbuffer[0] == null )
                            break;
                        else
                        if( subbuffer[0].length() <= 1 )
                            break;

                        WebViewDataStack.PushLast( subbuffer[0] );

                    }while(true);


                }
                else
                if( PeekLastStr.indexOf( "***FINISHED***" ) >= 0 )
                {
                    //PopAddTumblrWebView(5);
                    ClearStacks();
                }
            }
            else
            if( searchtype == AUTHOR_SEARCH )
            {
                if( PeekLastStr == null )
                {
                    ProgressDialogObj = ProgressDialog.show( MainActivityObj, "", "Downloading..." );
                    if( KeyWordStr.indexOf(".com") < 0 )
                        KeyWordStr += ".tumblr.com";

                    url = "https://api.tumblr.com/v2/blog/" + urlencode(KeyWordStr) + "/posts/photo?notes_info=false&api_key=" + api_key;

                    InternetObj.GotoUrl(url , Internet.HTTP_GET, "", (short)0 );
                    UrlStack.PushLast( url );

                    ImagesFound = 0;
                }
                else
                if( PeekLastStr.indexOf( "https://api.tumblr.com" ) >= 0 )
                {
                    ProgressDialogObj.dismiss();
                    ItsTimerTask.SetSpeed(10);
                    String buffer[] = { ResponseStack.PopLast() };
                    UrlStack.PopLast();

                    String[] subbuffer = { "" };

                    if( buffer[0].indexOf( "\"status\":404" ) < 0 || buffer[0].length() <= 1 )
                    {
                        do
                        {
                            subbuffer[0] = ExtractBraceGroupJson( buffer, "{\"blog_name\":\"", '{', '}' );

                            if( subbuffer[0] == null )
                                break;
                            else
                            if( subbuffer[0].length() <= 1 )
                                break;

                            WebViewDataStack.PushLast( subbuffer[0] );
                            ImagesFound++;

                        }while(true);
                    }
                    if( ImagesFound == 0 )
                        AddErrorPage();
                    else
                        PopAddTumblrWebView(3);

                    ClearStacks();
                }
            }

            ItsTimerTask.SetSpeed(1);
        }
        //--------------------------------------------------------------------------------------------------
        protected void ClearStacks()
        {
            blogidstack = new CustomStack<String>();
            postidstack = new CustomStack<String>();
            UrlStack = new CustomStack<String>();
            ResponseStack = new CustomStack<String>();
        }
        //--------------------------------------------------------------------------------------------------
        public void PopAddTumblrWebView( int count )
        {
            String[] subbuffer = { "" };
            int i;

            for( i = 0; i < count; i++ )
            {
                subbuffer[0] = WebViewDataStack.PopLast();
                if( subbuffer[0] == null )
                    break;
                MainActivityObj.AddTumblrWebView( CreatePost( subbuffer ));
            }
        }
        //--------------------------------------------------------------------------------------------------
        public void AddErrorPage()
        {
            String[] errorpage = { "<center><img src=\"data:image/jpeg;base64,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\" /></cemter>" };

            MainActivityObj.AddTumblrWebView( errorpage );
        }
        //--------------------------------------------------------------------------------------------------
        public void ClearTumblrWebViewDataStack()
        {
            WebViewDataStack.Clear();
        }
        //--------------------------------------------------------------------------------------------------
        String ExtractBraceGroupJson( String[] datastr, String substrstart, char charopen, char charclose )
        {
            int length = datastr[0].length();
            int i, bracecount = 1;
            int startindex = datastr[0].indexOf(substrstart);
            int endindex = 0;
            String[] substrdelete = { "" };
            String spacestr = "";

            if( startindex < 0 )
                return null;

            for( i = startindex + 1; i < length; i++ )
            {
                if( datastr[0].charAt(i) == charopen )
                    bracecount++;
                else
                if( datastr[0].charAt(i) == charclose )
                    bracecount--;
                if( bracecount == 0 )
                {
                    endindex = i;
                    break;
                }
            }

            substrdelete[0] = new String( datastr[0].substring( startindex, endindex ) );
            spacestr = repeat(spacestr, substrdelete[0].length());

            datastr[0] = datastr[0].replaceFirst( Pattern.quote(substrdelete[0]), spacestr );

            return substrdelete[0];
        }
        //--------------------------------------------------------------------------------------------------
        String[] CreatePost( String[] blogdata )
        {
            String blogname = StrCut( "\"blog_name\":\"", "\"", blogdata );
            String blogtype = StrCut( "\"type\":\"", "\"", blogdata );
            String blogdate = StrCut( "\"date\":\"", "\"", blogdata );

            String blogtags = StrCut( "\"tags\":[", "]", blogdata );

            if( blogtags != null )
                blogtags = blogtags.replace("\"", "" );
            else
                blogtags = "";

            String blogcaption = StrCut( "\"caption\":\"", "\"", blogdata );

            String[] blogimagelist = new String[7];
            String blogimagelink;

            int i;
            for( i = 0; i < 7; i++ )
            {
                blogimagelist[i] = "";
                blogimagelink = StrCut( "{\"url\":\"", "\"", blogdata );
                if( blogimagelink == null )
                    break;
                blogimagelist[i] = blogimagelink;
            }

            String[] outputhtml = { "<center><b>" + blogname + "</b> " + blogdate + "<br><i>" + blogtags+ "</i><br>" + blogcaption + "<br>"
                    + "<a href=\"" + blogimagelist[0] + "\" >" + "<img src=\"" + blogimagelist[2] + "\" /></a></center>" };

            return outputhtml;

        }
        //--------------------------------------------------------------------------------------------------
        protected void ExtractPhotoPostsSearch( String buffer, CustomStack<String> blogidstack, CustomStack<String> postidstack )
        {
            int i;
            String[] Buffer = { buffer };
            String[] posturl = new String[1];
            String blogid = "", postid = "", returnstr = "";

            if( Buffer[0] == null )
                return;
            if( Buffer[0].length() <= 1 )
                return;

            Buffer[0] = InternetObj.Decode(Buffer[0]);

            for( i = 0; i < 500; i++ )
            {
                if( StrCut( "class=\"photo\"", "src=", Buffer ) == null )
                    break;

                posturl[0] = StrCut( "data-pin-url=\"", "\"", Buffer );

                if( posturl[0] == null )
                    break;

                blogid = StrCut( "http://", "/", posturl );

                if( StrCountChar( posturl[0], "/" ) >= 2 )
                    postid = StrCut( "post/", "/", posturl );
                else
                {
                    posturl[0] = posturl[0] + "\"";
                    postid = StrCut( "post/", "\"", posturl );
                }

                if( returnstr.indexOf( postid ) < 0 )
                {
                    returnstr += blogid + "," + postid + ";";
                    blogidstack.PushLast(blogid);
                    postidstack.PushLast(postid);
                }
            }

            //return returnstr;
        }
        //--------------------------------------------------------------------------------------------------
        String CreateTumblrApiUrl( String blogid, String postid, String posttype )
        {
            return "https://api.tumblr.com/v2/blog/" + blogid +  "/posts/" + posttype + "?api_key=" + api_key;
        }
        //--------------------------------------------------------------------------------------------------
        public void Do()
        {


            if( InternetObj.ByteArrayListStatus[0] == Internet.STATUS_DONE )
            {
                String buffer[] = { "" };
                InternetObj.ByteArrayListStatus[0] = Internet.STATUS_EMPTY;

                try
                {
                    buffer[0] =  new String(InternetObj.ByteArrayList[0], "UTF-8");
                    buffer[0] = InternetObj.Decode(buffer[0]);
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }

                if( buffer[0].length() >= 60 )
                {
                    ResponseStack.PushLast( buffer[0] );
                    Search( KeyWordStr, SearchType );
                }
                else
                {
                    if( ProgressDialogObj != null )
                        ProgressDialogObj.dismiss();
                    AddErrorPage();
                    ClearStacks();
                }
                return;
            }
            else
            if( InternetObj.ByteArrayListStatus[0] == Internet.STATUS_NULL_STREAM )
            {
                String buffer[] = { "" };
                InternetObj.ByteArrayListStatus[0] = Internet.STATUS_EMPTY;
                ResponseStack.PushLast( "" );

                if( ProgressDialogObj != null )
                    ProgressDialogObj.dismiss();

                //AddErrorPage();
                ClearStacks();

                //Search( KeyWordStr, SearchType );
                return;
            }

            if( InternetObj.ByteArrayListStatus[1] == Internet.STATUS_DONE )
            {
                String buffer[] = { "" };
                InternetObj.ByteArrayListStatus[1] = Internet.STATUS_EMPTY;

                try
                {
                    buffer[0] =  new String(InternetObj.ByteArrayList[1], "UTF-8");
                    buffer[0] = InternetObj.Decode(buffer[0]);
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }

                if( InternetObj.HeaderList == null )
                    throw new RuntimeException( "InternetObj.HeaderList nullpointer!" );
                if( InternetObj.HeaderList[1] == null )
                    throw new RuntimeException( "InternetObj.HeaderList[1] nullpointer!" );
                if( InternetObj.HeaderList[1][0] == null )
                    throw new RuntimeException( "InternetObj.HeaderList[1][0] nullpointer!" );
                if( InternetObj.ByteArrayList[1] == null )
                    throw new RuntimeException( "InternetObj.ByteArrayList[1] nullpointer!" );

                if( InternetObj.HeaderList[1][0].length() > 1 || buffer[0].length() > 1 )
                {
                    OAuthDo( buffer[0] );
                }
                else
                {
                    buffer[0] = "Connection error!" + "(" + OAuthStep + ")";
                    MainActivityObj.AddTumblrWebView( buffer );
                }
                return;
            }
            else
            if( InternetObj.ByteArrayListStatus[1] == Internet.STATUS_NULL_STREAM )
            {
                String buffer[] = { "" };
                InternetObj.ByteArrayListStatus[1] = Internet.STATUS_EMPTY;

                //AddErrorPage();

                if( InternetObj.HeaderList == null )
                    throw new RuntimeException( "InternetObj.HeaderList nullpointer!!" );
                if( InternetObj.HeaderList[1] == null )
                    throw new RuntimeException( "InternetObj.HeaderList[1] nullpointer!!" );
                if( InternetObj.HeaderList[1][0] == null )
                    throw new RuntimeException( "InternetObj.HeaderList[1][0] nullpointer!!" );
                if( InternetObj.ByteArrayList[1] == null )
                    throw new RuntimeException( "InternetObj.ByteArrayList[1] nullpointer!" );

                buffer[0] = "Status null stream: [" + InternetObj.HeaderList[1][0] + "] [" + buffer[0] + "]";
                MainActivityObj.AddTumblrWebView( buffer );
                return;
            }

        }
        //--------------------------------------------------------------------------------------------------
        public void OAuthDo( String inputbuffer )
        {

            String args = "", url = "";
            String[] redirecturl = { "" };
            String[] outputstr = { "" };
            String[] inputbuffercopy = { new String(inputbuffer) };

            if( OAuthStep == -5 )
            {
                url = "https://posttestserver.com/post.php";
                InternetObj.GotoUrl( url, Internet.HTTP_POST, "argument%5B1%5D=1&argument%5B2%5D=2&argument%5B1%5D=3&feces=14", (short)1 );
                OAuthStep++;

            }
            else
            if( OAuthStep == -4 )
            {
                outputstr[0] = InternetObj.HeaderList[1][0] + ";" + inputbuffer;
                MainActivityObj.AddTumblrWebView(outputstr);

                OAuthStep = -1;
            }
            else
            if( OAuthStep == 0 )
            {
                url = "https://www.tumblr.com/login";
                InternetObj.GotoUrl( url, Internet.HTTP_GET, "", (short)1 );
                OAuthStep++;
            }
            else
            if( OAuthStep == 1 )
            {
                outputstr[0] = "***[1]***" +  InternetObj.HeaderList[1][0] + ";" + InternetObj.ItsCookie + ";";
                MainActivityObj.AddTumblrWebView(outputstr);

                if( inputbuffer.indexOf("determine_email") >= 0 )
                {
                    String useremail = urlencode("blank email");
                    String userpassword = "";
                    form_key = urlencode(StrCut( "name=\"form_key\" value=\"", "\"", inputbuffercopy ));

                    args = "determine_email=" + useremail + "&user%5Bemail%5D=&user%5Bpassword%5D=" + userpassword
                            + "&tumblelog%5Bname%5D=&user%5Bage%5D=&context=no_referer&version=STANDARD&follow="
                            + "&form_key=" + form_key + "&seen_suggestion=0&used_suggestion=0&used_auto_suggestion=0"
                            + "&about_tumblr_slide=&random_username_suggestions=" + urlencode("[\"RainyHarmonyCollector\",\"AnnoyingGladiatorStudent\",\"NumberOneBouquetWonderland\",\"InstantPersonCollection\",\"ImpossibleUnknownCollection\"]")
                            + "action=signup_determine&tracking_url=" + urlencode("/login") + "&tracking_version=modal";

                    url = "https://www.tumblr.com/svc/account/register";

                    outputstr[0] = "1 - " + args;
/*
    if( inputbuffer.indexOf("captcha") >= 0 )
     	outputstr[0] += args + "[captcha encountered!]";
*/
                    MainActivityObj.AddTumblrWebView(outputstr);

                    InternetObj.GotoUrl( url, Internet.HTTP_POST, args, (short)1 );
                    OAuthStep++;
                }
                else
                    OAuthStep = -1;
            }
            else
            if( OAuthStep == 2 )
            {
                outputstr[0] = "***[2]***" +  InternetObj.HeaderList[1][0] + ";" + InternetObj.ItsCookie + ";";
                MainActivityObj.AddTumblrWebView(outputstr);

                if( inputbuffer.indexOf("next_view\":\"signup_login\"}") >= 0 )
                {
                    String useremail = urlencode("blank email");
                    String userpassword = urlencode("blank pasword");

                    args = "determine_email=" + useremail + "&user%5Bemail%5D=" + useremail + "&user%5Bpassword%5D=" + userpassword
                            + "&tumblelog%5Bname%5D=&user%5Bage%5D=&context=no_referer&version=STANDARD&follow="
                            + "&form_key=" + form_key + "&seen_suggestion=0&used_suggestion=0&used_auto_suggestion=0"
                            + "&about_tumblr_slide=&random_username_suggestions=" + urlencode("[\"EclecticStarlightDonut\",\"CasuallyAutomaticNight\",\"CasuallyGenerousGlitter\",\"DangerouslyCoolVoid\",\"DelectablyImpossibleCherryblossom\"]");

                    url = "https://www.tumblr.com/login";

                    outputstr[0] = "1 - " + args;
/*
    if( inputbuffer.indexOf("captcha") >= 0 )
     	outputstr[0] += args + "[captcha encountered!]";
*/
                    MainActivityObj.AddTumblrWebView(outputstr);

                    InternetObj.GotoUrl( url, Internet.HTTP_POST, args, (short)1 );
                    OAuthStep++;
                }
                else
                    OAuthStep = -1;
            }
            else
            if( OAuthStep == 3 )
            {
                outputstr[0] = "***[3]***" +  InternetObj.HeaderList[1][0] + ";" + InternetObj.ItsCookie;
                outputstr[0] = outputstr[0].replace( "<", "(" );
                outputstr[0] = outputstr[0].replace( ">", ")" );
                MainActivityObj.AddTumblrWebView(outputstr);

                if( inputbuffer.indexOf("dashboard-context") >= 0 )
                {
                    //outputstr[0] = "***[2]***" +  InternetObj.HeaderList[1][0] + ";";
                    //MainActivityObj.AddTumblrWebView(outputstr);

                    url = "https://api.tumblr.com/console/auth";
                    args = "consumer_key=" + consumer_key + "&consumer_secret=" + consumer_secret;

                    InternetObj.GotoUrl( url, Internet.HTTP_POST, args, (short)1 );
                    OAuthStep++;

                    outputstr[0] = outputstr[0].replace( "<", "(" );
                    outputstr[0] = outputstr[0].replace( ">", ")" );
                }
                else
                    OAuthStep = -1;
            }
            else
            if( OAuthStep == 4 )
            {
                outputstr[0] = "***[3]***" +  InternetObj.HeaderList[1][0] + ";";
                MainActivityObj.AddTumblrWebView(outputstr);

                if( InternetObj.HeaderList[1][0] != null )
                    if( InternetObj.HeaderList[1][0].indexOf("Location") >= 0 )
                    {
                        redirecturl[0] = StrCut("Location, [", "]", InternetObj.HeaderList[1] );
                        args = "consumer_key=" + consumer_key + "&consumer_secret=" + consumer_secret;
                        InternetObj.GotoUrl( redirecturl[0], Internet.HTTP_POST, args, (short)1 );
                        MainActivityObj.AddTumblrWebView(redirecturl);
                        OAuthStep++;
                    }
                //else
                OAuthStep = -1;
/*
	String headerstr;
	String bufferstr;

	if( InternetObj.HeaderList[1][0] == null )
		headerstr = "[EMPTY HEADER]";
	else
		headerstr = InternetObj.HeaderList[1][0];

	if( OAuthDoInputBuffer == null )
		bufferstr = "[EMPTY BUFFER]";
	else
		bufferstr = new String(OAuthDoInputBuffer);

	String[] teststr = { new String(InternetObj.HeaderList[1][0] + bufferstr ) };
    teststr[0] = teststr[0] .replace( "<", "(" );
	teststr[0]= teststr[0] .replace( ">", ")" );
*/

	/*
	outputstr[0] = StrCut("<form","</form>",teststr);
	outputstr[0] = StrCut("<form","</form>",teststr);

	outputstr[0] = outputstr[0].replace( "<", "(" );
	outputstr[0] = outputstr[0].replace( ">", ")" );
	*/
                //String form_key = StrCut( "<input type=\"hidden\" name=\"form_key\" value=\"", "\"", OAuthDoInputBuffer );
                //String oauth_token = StrCut( "name=\"oauth_token\" value=\"", "\"", OAuthDoInputBuffer );
	/*
	if( form_key != null && oauth_token != null )
	{
	   args = "allow=&form_key=" + form_key + "&oauth_token=" + oauth_token;
	   url = "https://www.tumblr.com/oauth/authorize?oauth_token=" + oausth_token;
	   InternetObj.GotoUrl( url, Internet.HTTP_POST, args, (short)1 );
	   OAuthStep++;
	}
	else
	  throw new RuntimeException( "form_key OR oauth_token is null! [" + args +  "]" );
	  MainActivityObj.AddTumblrWebView(teststr);
		*/


            }
            else
            if( OAuthStep == 5 )
            {
                String[] formstr = { StrCut("<form","</form>", inputbuffercopy) };

                //formstr[0] = formstr[0].replace( "<", "(" );
                //formstr[0] = formstr[0].replace( ">", ")" );
                formstr[0] = InternetObj.HeaderList[1][0]; //+ formstr[0];

                formstr[0] = formstr[0].replace( "<", "(" );
                formstr[0] = formstr[0].replace( ">", ")" );

                //String[] formstr = { InternetObj.HeaderList[1][0] };
                MainActivityObj.AddTumblrWebView( formstr );
                OAuthStep++;
            }
        }
//--------------------------------------------------------------------------------------------------
    }
    //--------------------------------------------------------------------------------------------------
//##################################################################################################
//--------------------------------------------------------------------------------------------------
    class CustomTimerTask extends TimerTask {

        private int Speed = 1;
        private int TickCounter = 0;
        public final int TICK_COUNTER_MAX = 1000;

        @Override
        public void run()
        {
            runOnUiThread( new Runnable()
            {

                @Override
                public void run()
                {
                    if( TickCounter % Speed == 0 )
                        MainActivity.MainActivityObj.DoInternet();

                    TickCounter = TickCounter + 1;
                    if( TickCounter > TICK_COUNTER_MAX )
                        TickCounter = 0;
                }
            });

        }
        //--------------------------------------------------------------------------------------------------
        public void SetSpeed( int speed )
        {
            if( speed > 0 )
                Speed = speed;
        }
        //--------------------------------------------------------------------------------------------------
    }

//--------------------------------------------------------------------------------------------------
//##################################################################################################
//--------------------------------------------------------------------------------------------------

    protected LinearLayout LinearLayout1;
    protected EditText EditText1;
    public static Internet InternetObj;
    public static MainActivity MainActivityObj;
    public static Tumblr TumblrObj;

    protected Timer ItsTimer;
    protected CustomTimerTask ItsTimerTask;

    protected AlertDialog ItsAlertDialog;
    public static String debugstr = "";

    //--------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout1 = (LinearLayout)findViewById(R.id.LinearLayout1);

        InternetObj= new Internet(this);
        MainActivityObj = this;
        TumblrObj = new Tumblr( MainActivityObj, InternetObj );

        ItsTimer = new Timer();
        ItsTimerTask = new CustomTimerTask();
        ItsTimer.schedule(ItsTimerTask, 100, 100);

        InitListeners();

        CreateAlertDialog();
    }
    //--------------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //--------------------------------------------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        boolean returnflag = false;

        if( id == R.id.action_clear_search )
        {
            ClearSearch();
            returnflag = true;
        }
        else
        if( id == R.id.action_save_images )
        {

            returnflag = true;
        }
        else
        if( id == R.id.action_about )
        {
            TumblrObj.OAuthStep = 0;
            TumblrObj.OAuthDo("");
            returnflag = true;
        }

        return super.onOptionsItemSelected(item);
    }

    //--------------------------------------------------------------------------------------------------
    public String repeat(String str, int times) {
        if (str == null) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < times ; i ++) {
            sb.append(str);
        }
        return sb.toString();
    }
    //--------------------------------------------------------------------------------------------------
    public String StrCut( String strhead, String strtail, String[] str )
    {
        int length1 = strhead.length();
        int length2 = strtail.length();

        int indexstart = str[0].indexOf(strhead);
        int indexend = str[0].indexOf(strtail, indexstart + length1);


        if ( indexstart >= 0 && indexend > indexstart )
        {

            String substrout = str[0].substring(indexstart + length1, indexend );
            String substrdelete = str[0].substring(indexstart, indexend + length2 );
            String spacestr = " ";

            spacestr = repeat(spacestr, substrdelete.length());

            str[0] = str[0].replaceFirst( Pattern.quote(substrdelete), spacestr );

            if( substrout == null )
                substrout = "";

            return substrout;
        }
        else
            return null;

    }
    //--------------------------------------------------------------------------------------------------
    private boolean CheckInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||

                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {
            Toast.makeText(this, " Not Connected ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }
    //--------------------------------------------------------------------------------------------------
    public int randint( int min, int max )
    {
        return (int)(Math.random() * ((max - min) + 1)) + min;
    }
    //--------------------------------------------------------------------------------------------------
    private void ReverseLinearLayout()
    {
        LinearLayout ll = LinearLayout1;
        ArrayList<View> views = new ArrayList<View>();

        for(int x = 0; x < ll.getChildCount(); x++) {
            views.add(ll.getChildAt(x));
        }
        ll.removeAllViews();
        for(int x = views.size() - 1; x >= 0; x--) {
            ll.addView(views.get(x));
        }
    }
    //--------------------------------------------------------------------------------------------------
    private void AddViewTopLinearLayout()
    {
        LinearLayout ll = LinearLayout1;
        ArrayList<View> views = new ArrayList<View>();
        int x;

        int layoutchildcount = ll.getChildCount();

        if( layoutchildcount >= 2 )
        {
            views.add(ll.getChildAt(layoutchildcount-1));

            for(x = 0; x < layoutchildcount - 1; x++)
                views.add(ll.getChildAt(x));

            ll.removeAllViews();
            for(x = 0; x < views.size(); x++)
                ll.addView(views.get(x));
        }

    }
    //--------------------------------------------------------------------------------------------------
    void AddTumblrWebView( String[] htmldata )
    {
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View tumblrview = inflater.inflate(R.layout.row, null);

/*
ImageView image1 = (ImageView)addview.findViewById(R.id.ImageViewB);

Bitmap bm_src = ((BitmapDrawable)ImageView1.getDrawable()).getBitmap();
Bitmap bm_dest = bm_src.copy(bm_src.getConfig(), true);

image1.setImageBitmap(bm_dest);
*/

        CustomWebView customwebview = (CustomWebView)tumblrview.findViewById(R.id.WebView1);

        String url = "";

        customwebview.loadDataWithBaseURL(url, htmldata[0], "text/html", "utf-8", null);

        customwebview.Data = new String[1];
        customwebview.Data[0] =  new String(htmldata[0]);

        LinearLayout1.addView(tumblrview);

        Button saveimagebutton = (Button)tumblrview.findViewById(R.id.saveimagebutton);
        saveimagebutton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                ((LinearLayout)tumblrview.getParent()).removeView(tumblrview);
            }});


        AddViewTopLinearLayout();
    }
    //--------------------------------------------------------------------------------------------------
    protected void InitListeners()
    {

        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !TumblrObj.CheckWorking() )
                {
                    TumblrObj.Search( "", TumblrObj.RESET_SEARCH );
                    TumblrObj.Search( GetEditText(), TumblrObj.PHOTO_EXPLORE );
                }
            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View v) {

                if( !TumblrObj.CheckWorking() )
                {
                    TumblrObj.Search( "", TumblrObj.RESET_SEARCH );
                    TumblrObj.Search( GetEditText(), TumblrObj.PHOTO_SEARCH );
                }
            }
        });

        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !TumblrObj.CheckWorking() )
                {
                    TumblrObj.Search( "", TumblrObj.RESET_SEARCH );
                    TumblrObj.Search( GetEditText(), TumblrObj.AUTHOR_SEARCH );
                }
            }
        });

        EditText EditText1 = (EditText) findViewById(R.id.EditText1);
        EditText1.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    if( !TumblrObj.CheckWorking() )
                    {
                        TumblrObj.Search( "", TumblrObj.RESET_SEARCH );
                        TumblrObj.Search( GetEditText(), TumblrObj.PHOTO_EXPLORE );
                    }
                    return true;
                }
                return false;
            }
        });
    }
    //--------------------------------------------------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getActionMasked();
        float x, y;

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                TumblrObj.PopAddTumblrWebView(3);
                break;

            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
    //--------------------------------------------------------------------------------------------------
    public void DoInternet()
    {
        TumblrObj.Do();
        InternetObj.ClearByteArrayListDoneStatus();

    }
    //--------------------------------------------------------------------------------------------------
    public void ClearSearch()
    {
        LinearLayout ll = LinearLayout1;
        ll.removeAllViews();
        TumblrObj.ClearTumblrWebViewDataStack();
    }
    //--------------------------------------------------------------------------------------------------
    public String GetEditText()
    {
        EditText EditText1 = (EditText)findViewById(R.id.EditText1);
        return EditText1.getText().toString();
    }
    //--------------------------------------------------------------------------------------------------
    public void SetEditText( String str )
    {
        EditText EditText1 = (EditText)findViewById(R.id.EditText1);
        EditText1.setText(str);
    }
    //--------------------------------------------------------------------------------------------------
    public void CreateAlertDialog()
    {
        AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(this);
        alertdialogbuilder.setMessage("blank message");
        alertdialogbuilder.setCancelable(true);

        alertdialogbuilder.setNeutralButton(
                "Okay",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        ItsAlertDialog = alertdialogbuilder.create();
        ItsAlertDialog.cancel();
    }
    //--------------------------------------------------------------------------------------------------
    public void ShowAlert( String message )
    {
        ItsAlertDialog.setMessage(message);
        ItsAlertDialog.show();
    }
//--------------------------------------------------------------------------------------------------
}
