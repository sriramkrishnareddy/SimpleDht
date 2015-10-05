package edu.buffalo.cse.cse486586.simpledht;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.os.AsyncTask;
import java.io.PrintWriter;
import android.app.Activity;
import java.net.InetAddress;
import android.content.Context;
import java.util.Iterator;
import android.telephony.TelephonyManager;

import org.apache.http.entity.StringEntity;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;

public class SimpleDhtProvider extends ContentProvider {
    public static String avd_number;
    Socket sock;
    InputStreamReader in;
    BufferedReader br;
    public static String nextNode;
    public static String previousNode;
    public static String nextNodeHash = "";
    public static String previousNodeHash = "";
    public static String thisNodeHash;
    public static String resultAfterSearch="";
    public static ArrayList<String> nodesInRing = new ArrayList<>();
    public static ArrayList<String> nodesAndKeys = new ArrayList<>();
    public static HashMap<String, String> hashValue_avdNumber = new HashMap<>();
    public static final String AUTH = "edu.buffalo.cse.cse486586.simpledht";
    public static final Uri URI = Uri.parse("content://edu.buffalo.cse.cse486586.simpledht.provider");
    databaseclass dbc;
    final static int com = 1;
    SQLiteDatabase db;
    public Context context;
    public static ContentResolver dhtCr;
    public static Uri dhtUri;


    public static final UriMatcher uri_matcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uri_matcher.addURI(AUTH, databaseclass.table_name, com);
    }

    public String getPreviousNodeNumber(HashMap<String, String> number_hash, ArrayList<String> nodeslist, int location) {

        if (location == 0) {
            System.out.println("location 0 raised");
            return number_hash.get(nodeslist.get(nodeslist.size() - 1));
        } else {
            return number_hash.get(nodeslist.get(location - 1));
        }
    }
    public String getCorrespondingNode(HashMap<String, String> hash_number, ArrayList<String> nodesKeysList, String keyHash, String newKey){
        System.out.println("Getting Corresponding node for key"+newKey);
        System.out.println("Size and contents of nodeskeyslist "+nodesKeysList.size());
        if(nodesKeysList.size()==0){
            System.out.println("zero size This belongs to me" +avd_number);
            //System.out.println("verrr" + nodesKeysList.get(0) + "-->" + nodesKeysList.get(1) + "-->" + nodesKeysList.get(2));
            return avd_number;
        }
        if(nodesKeysList.size()==1&&avd_number.equals("5554")){
            System.out.println("zero size This belongs to me" +avd_number);
            return avd_number;
        }
        String CorrespondingNode;
        nodesKeysList.add(keyHash);
//        if(nodesKeysList.size()==1){
//            System.out.println("This belongs to me" +avd_number);
//            //System.out.println("verrr" + nodesKeysList.get(0) + "-->" + nodesKeysList.get(1) + "-->" + nodesKeysList.get(2));
//            return avd_number;
//        }
        Collections.sort(nodesKeysList);
        System.out.println("verrr" + nodesKeysList.get(0) + "-->" + nodesKeysList.get(1) + "-->" + nodesKeysList.get(2));
        System.out.println("");
        for(String e:nodesKeysList)
            System.out.print("NodesKeysList" + e);
        System.out.println("");
        int keyIndex = nodesKeysList.indexOf(keyHash);
        if(keyIndex==nodesKeysList.size()-1){
            CorrespondingNode = hash_number.get(nodesKeysList.get(0));
            nodesKeysList.remove(keyHash);
            System.out.println("crrr"+CorrespondingNode);
            return CorrespondingNode;
        }
        else {
            CorrespondingNode = hash_number.get(nodesKeysList.get(keyIndex+1));
            nodesKeysList.remove(keyHash);
            System.out.println("crrr"+CorrespondingNode);
            return CorrespondingNode;
        }
    }



    public String getNextNodeNumber(HashMap<String, String> number_hash, ArrayList<String> nodeslist, int location) {
        if (location == nodeslist.size() - 1) {
            System.out.println("location end raised");
            return number_hash.get(nodeslist.get(0));
        } else {
            return number_hash.get(nodeslist.get(location + 1));
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        db = dbc.getWritableDatabase();
        System.out.println("Deleting Statement is"+selection);
        selection = "key ='" + selection + "'";
        db.delete(databaseclass.table_name, selection, selectionArgs);
        Log.v("delete ", selection);
        System.out.println("count is"+count);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        System.out.println("New key is being inserted :" + values.get("key"));
        String newKey = values.get("key").toString();
        String newValue = values.get("value").toString();
        String newKeyHash = "";
        try {
            newKeyHash = genHash(newKey);
        } catch (NoSuchAlgorithmException e) {
            Log.e("Error", "Insertion");
        }
        System.out.println("qwertyqwerty");
//        System.out.println("HASHHASH newkeyhash"+newKeyHash);
//        System.out.println(nextNode+"HASHHASH previoushash"+previousNodeHash);
//        System.out.println(previousNode+"HASHHASH nexthash"+nextNodeHash);
//        System.out.println("Verification"+hashValue_avdNumber.size()+"-->"+nodesAndKeys.size()+"-->"+newKeyHash+"-->"+avd_number+"-->"+getCorrespondingNode(hashValue_avdNumber,nodesAndKeys,newKeyHash));

        String correspondingAvd = getCorrespondingNode(hashValue_avdNumber,nodesAndKeys,newKeyHash,newKey);
        System.out.println(newKey+":Correspondingnodeis:"+correspondingAvd+":presentavd:"+avd_number);
        if(!newKeyHash.equals("") && correspondingAvd.equals(avd_number)){
            System.out.println(newKey+":Correspondingnodeis:"+correspondingAvd+":presentavd:"+avd_number);
            System.out.println("keyinsertedat "+correspondingAvd +newKey);
            db = dbc.getWritableDatabase();
            db.insert(databaseclass.table_name, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            Log.v("insert", values.toString());
            return uri;
        }
        else{
            String delimeter = "sriramreddy";
            System.out.println("keyinsertedat "+correspondingAvd +newKey);
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, correspondingAvd+delimeter+"correspondingavd"+delimeter+newKey+delimeter+newValue+delimeter+newKeyHash);
            return null;
        }

//        if (!newKeyHash.equals("") && newKeyHash.compareTo(previousNodeHash) > 0 && newKeyHash.compareTo(thisNodeHash) < 0)
//        //writing into content provider
//        {
//            System.out.println("This is 1st if");
//            db = dbc.getWritableDatabase();
//            db.insert(databaseclass.table_name, null, values);
//            getContext().getContentResolver().notifyChange(uri, null);
//            Log.v("insert", values.toString());
//            return uri;
//        }
//
//
//    else if(!newKeyHash.equals("") && newKeyHash.compareTo(previousNodeHash) > 0 && newKeyHash.compareTo(thisNodeHash) > 0){
//            System.out.println("This is 2nd if");
//            db = dbc.getWritableDatabase();
//            db.insert(databaseclass.table_name, null, values);
//            getContext().getContentResolver().notifyChange(uri, null);
//            Log.v("insert", values.toString());
//            return uri;
//        }
//        else{
//            System.out.println("This is final if");
//            System.out.println("This key do not belong to this node passing to next node" +thisNodeHash);
//            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "5554directthiskey",newKeyHash,newKey,newValue);
//
//        }
//            db = dbc.getWritableDatabase();
//            db.insert(databaseclass.table_name, null, values);
//            getContext().getContentResolver().notifyChange(uri, null);
//            Log.v("insert", values.toString());
            //return uri;
            //return uri;
}
    @Override
    public boolean onCreate() {



        dbc = new databaseclass(getContext());
        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(10000);
            // serverSocket.setSoTimeout(500);
            System.out.println("Server created and I started");
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);


        } catch (IOException e) {
            Log.e(e+"", "exce Error Can't creeate ServerSockoet");
            return true;
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        String searchKey="";
        String searchKeyHash="";
        final String correspondingAvd;
        System.out.println("selection statement is"+selection);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(databaseclass.table_name);
        Cursor cursor;
        if(!selection.equals("\"*\"")&&!selection.equals("\"@\"")) {

            if (selection != null) {
                searchKey = selection;
                selection = "key ='" + selection + "'";
            }
            try{
                searchKeyHash = genHash(searchKey);
            }
            catch (NoSuchAlgorithmException e){Log.e("Error:"+e,"NSAE");}
            correspondingAvd = getCorrespondingNode(hashValue_avdNumber,nodesAndKeys,searchKeyHash,searchKey);

            if(correspondingAvd.equals(avd_number)) {
                cursor = qb.query(dbc.getReadableDatabase(),
                        projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                Log.v("query", selection);
                return cursor;
            }

            else{
                System.out.println(searchKey+"correspondsto"+correspondingAvd);
//                Thread t1 = new Thread(new Callable<Cursor>() {
//
//                    public Cursor call() {
//                        System.out.println("I'm new thread");
//                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,correspondingAvd+"handlethisquery"+SimpleDhtActivity.getAvd_number());
//                        MatrixCursor filledcursor = new MatrixCursor(new String[] {"key","value"});
//                        filledcursor.addRow(new String[] {"key0","value0"});
//                        return filledcursor;
//                    }
//
//                });
//                t1.start();
//                try{
//                t1.wait();
//                }catch(InterruptedException e){Log.e("Error"+e,"Wait Error");}

//                Thread t1 = new Thread(new Callable<String>()){
//
//                }
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, correspondingAvd + "handlethisquery"+avd_number+ searchKey);
                synchronized (this){
                    try{
                        this.wait();
                    }catch(InterruptedException e){Log.e("Error"+e,"Interrupted in Query");}
                }
//                System.out.println("resultAfterSearch="+resultAfterSearch);
//                String[] keyValuePairAfterSearch = resultAfterSearch.split(",");
//                MatrixCursor filledcursor = new MatrixCursor(new String[] {"key","value"});
//                filledcursor.addRow(new String[] {keyValuePairAfterSearch[0].substring(17),keyValuePairAfterSearch[1]});
//                return filledcursor;
//                System.out.println("resultAfterSearched="+keyValuePairAfterSearch[0].substring(4)+" "+keyValuePairAfterSearch[1]);
//---------------------------------------WORKING CODE----------------------------------------------------
                ContentValues cv = new ContentValues();
                String[] hackKeyValuePairs = resultAfterSearch.split(",");
                cv.put("key",hackKeyValuePairs[0].substring(17));
                cv.put("value",hackKeyValuePairs[1]);
            db = dbc.getWritableDatabase();
            db.insert(databaseclass.table_name, null, cv);
            getContext().getContentResolver().notifyChange(uri, null);

                cursor = qb.query(dbc.getReadableDatabase(),
                        projection, selection, selectionArgs, null, null, sortOrder);

                return cursor;
//------------------------------------WORKING CODE--------------------------------------------------------

            }
        }
        else if(selection.equals("\"*\"")){
            cursor = qb.query(dbc.getReadableDatabase(),
                    null, null, null, null, null, null);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            Log.v("query", "Local Dump");
            return cursor;
            //System.out.println("Must Implement This Global Selection");
        }
        else if(selection.equals("\"@\"")){
            cursor = qb.query(dbc.getReadableDatabase(),
                    null, null, null, null, null, null);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            Log.v("query", "Local Dump");
            return cursor;
        }
return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public SimpleDhtProvider(Context context){
            this.context=context;


        avd_number = SimpleDhtActivity.getAvd_number();
        System.out.println("Avd Number is "+avd_number);
        try{
            thisNodeHash = genHash(avd_number);
            System.out.println("This node hash assigned");

        }
        catch(NoSuchAlgorithmException e){Log.e("Constructor","No Such Algo"); }
               if(!avd_number.equals("5554")){
                   System.out.println("5554isnotthenewnode");
                   new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"newnode"+SimpleDhtActivity.getAvd_number());
               }

                else if(avd_number.equals("5554")){
                   System.out.println("I'm the 1st Node "+avd_number);
                   nextNode = 5554+"";
                   previousNode = 5554+"";
                   nodesInRing.add(thisNodeHash);
                   nodesAndKeys.add(thisNodeHash);
                   hashValue_avdNumber.put(thisNodeHash, "5554");
                   Collections.sort(nodesInRing);


               }


    }
    public SimpleDhtProvider(){

        //Do nothing
        //Just to avoid has no zero argument constructor error
    }



    public class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            final ServerSocket serverSocket = sockets[0];
            String newNodeHash="";

            try {
                while(true) {
                sock = serverSocket.accept();
                in= new InputStreamReader(sock.getInputStream());
                    br = new BufferedReader(in);
                    String tempmess = br.readLine();
                    System.out.println("tempmess"+tempmess);
                    if(tempmess.contains("handlethisquery")){
                        String searchKey = tempmess.substring(23);
                        StringBuffer resultKey=new StringBuffer("");
                        String redirectToAvd = tempmess.substring(19,23);
                        Cursor resultCursor = query(URI, null,searchKey, null, null);
                        int keyIndex = resultCursor.getColumnIndex("key");
                        int valueIndex = resultCursor.getColumnIndex("value");
                        if(resultCursor.moveToFirst()){
                            String rkey;
                            String rval;
                            do{
                                rkey = resultCursor.getString(keyIndex);
                                rval = resultCursor.getString(valueIndex);
                                System.out.println("Sriram: key = "+rkey+" Value= "+rval);
                                resultKey.append(rkey+","+rval);
                            }while(resultCursor.moveToNext());
                        }

                        System.out.println("resultqueryis"+resultKey.toString());
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,redirectToAvd+"resultqueryis"+resultKey);
                    }
                    else if(tempmess.contains("resultqueryis")){
                        resultAfterSearch = tempmess;
                        System.out.println("callingpublishprogress"+avd_number);


                        synchronized (SimpleDhtProvider.this) {

                            SimpleDhtProvider.this.notify();

                        }



                    }
                    else if(tempmess.contains("newnode")){
                        String newNodeAvdNumber = tempmess.substring(7);
                        try {
                            newNodeHash = genHash(newNodeAvdNumber);
                        }catch(NoSuchAlgorithmException e){}
                            nodesInRing.add(newNodeHash);
                            nodesAndKeys.add(newNodeHash);
                            hashValue_avdNumber.put(newNodeHash,tempmess.substring(7));
                            Collections.sort(nodesInRing);
                            Collections.sort(nodesAndKeys);
                            int i = nodesInRing.indexOf(newNodeHash);
                        System.out.println("New Node added Node ring size is"+nodesInRing.size()+" hash size is "+hashValue_avdNumber.size());
                        for(String e:nodesInRing)
                            System.out.println(e);
                        String newNodeNextNode = getNextNodeNumber(hashValue_avdNumber,nodesInRing,i);
                        String newNodePreviousNode = getPreviousNodeNumber(hashValue_avdNumber,nodesInRing,i);


                        for(String key:hashValue_avdNumber.keySet()) {
                            StringBuilder sb = new StringBuilder();
                            for(String keys:hashValue_avdNumber.keySet()){
                                sb.append(",");
                                sb.append(hashValue_avdNumber.get(keys));

                            }
                            System.out.println("Keys in ring"+sb);
                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, hashValue_avdNumber.get(key) + "updatenodesinring"+sb.toString());
                        }
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, tempmess.substring(7)+" changepreviousandnext "+newNodePreviousNode+newNodeNextNode);
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, newNodePreviousNode+" changepreviousnext "+tempmess.substring(7));
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, newNodeNextNode+" changenextprevious "+tempmess.substring(7));
                    }
                    else if(tempmess.contains(" changepreviousandnext ")){
                        //Change Next and Previous of New Node
                        previousNode = tempmess.substring(27,31);
                        nextNode = tempmess.substring(31,35);
                        System.out.println(tempmess.substring(0,4)+"My Next Node is "+nextNode);
                        System.out.println(tempmess.substring(0,4)+"My Previous Node is"+previousNode);
                        try{
                        previousNodeHash = genHash(previousNode);

                        nextNodeHash = genHash(nextNode);
                        }catch(NoSuchAlgorithmException e){Log.e("Error","NSAE");}
                    }
                    else if(tempmess.contains(" changepreviousnext ")){
                        //Change Previous node's next to New Node
                        nextNode=tempmess.substring(24,28);
                        System.out.println(tempmess.substring(0,4)+"My next Node is "+nextNode);
                        try{
                             nextNodeHash = genHash(nextNode);
                        }catch(NoSuchAlgorithmException e){Log.e("Error","NSAE");}
                    }
                    else if(tempmess.contains(" changenextprevious ")){
                        //Change Next node's previous to New Node
                        previousNode=tempmess.substring(24,28);
                        System.out.println(tempmess.substring(0,4)+"My Previous node is "+previousNode);
                        try{
                            previousNodeHash = genHash(previousNode);
                        }catch(NoSuchAlgorithmException e){Log.e("Error","NSAE");}
                    }
                    else if(tempmess.contains("5554directthiskey")) {
                        String[] scrapHashKeyValue = tempmess.split("sriramreddy");
                        //System.out.println("keyandvalue Key: "+keyandvalue[2]+" Value: "+keyandvalue[3]);
                        String CorrespondingNode=getCorrespondingNode(hashValue_avdNumber,nodesAndKeys,scrapHashKeyValue[1],scrapHashKeyValue[2]);
                        System.out.println("Corresponding Node is " + CorrespondingNode);

                    }
                    else if(tempmess.contains("updatenodesinring")){
                        String[] updateList = tempmess.split(",");
                        for(String z:updateList)
                            if(z.length()==4) {
                                if (!hashValue_avdNumber.containsValue(z)) {
                                    try {
                                        String updateHash = genHash(z);
                                        hashValue_avdNumber.put(updateHash,z);
                                        nodesInRing.add(updateHash);
                                        nodesAndKeys.add(updateHash);
                                        System.out.println("updatedthisnode");
                                        for(String x:nodesInRing)
                                            System.out.println("updatedthisnode"+x);
                                    } catch (NoSuchAlgorithmException e) {
                                        Log.e("Error:", "NSAE");
                                    }
                                }
                            }

                        System.out.println("updatenodesinring"+tempmess);

                    }

                    else if(tempmess.contains("correspondingavd")){
                        String[] avdNumberscrapKeyValueHash = tempmess.split("sriramreddy");
                        ContentValues cv = new ContentValues();
                        cv.put("key",avdNumberscrapKeyValueHash[2]);
                        cv.put("value",avdNumberscrapKeyValueHash[3]);
                        insert(URI,cv);
                        System.out.println("This key and value belongs to you Mr."+avdNumberscrapKeyValueHash[0]+" key="+avdNumberscrapKeyValueHash[2]+" value="+avdNumberscrapKeyValueHash[3]);


                    }



//                    if(!tempmess.contains("5554")) {
//                        publishProgress(tempmess);
//                        System.out.println("New Node Joining: " + tempmess.substring(7));
//                        System.out.println("Server running on node:" + SimpleDhtActivity.getAvd_number());
//                        try {
//                            newNodeHash = genHash(tempmess.substring(7));
//                            thisNodeHash = genHash(SimpleDhtActivity.getAvd_number());
//                            System.out.println("Switch executed" + newNodeHash + "__" + thisNodeHash);
//                            switch (thisNodeHash.compareTo(newNodeHash)) {
//                                case 1:
//
//                                    if (nextNode == 5554) {//loop has completed and reached again
//                                        System.out.println("My next node is 5554 I completed a loop");
//                                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "5554 change ur previous node" + SimpleDhtActivity.getAvd_number());
//                                        nextNode = Integer.parseInt(tempmess.substring(7));
//
//                                    }
//                                    System.out.println("This is greater " + thisNodeHash);
//                                    break;
//                                case -1:
//                                    if (previousNode == 5554) {//loop has completed and reached again
//                                        System.out.println("My previous node is 5554 I completed a loop");
//                                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "5554 change ur next node" + SimpleDhtActivity.getAvd_number());
//                                        previousNode = Integer.parseInt(tempmess.substring(7));
//                                    }
//                                    System.out.println("This is greater" + newNodeHash);
//                                    break;
//                                case 0:
//                                    System.out.println("Watch Out Sriram Both Nodes Should Not Be Equal");
//                                    break;
//                                default:
//                                    System.out.println("Watch out Sriram this is Fishy");
//                                    break;
//                            }
//
//
//                        } catch (NoSuchAlgorithmException e) {
//                            Log.e("Server Error:", "No Such Algorithm");
//                        }
//                    }
//
//                    else if(tempmess.contains("5554")){
//
//                    }
                }
            }
            catch (UnknownHostException e){
                Log.e("Unknown Host"+e, "exce error Error ServerTask socket SocketTimeOutException");
                System.out.println("Error" + e);
            }
            catch (IOException e) {
                Log.e("IOException"+e, "exce error Error ServerTask socket IOException");
                System.out.println("Error" + e);
            }
            return null;
        }

        protected void onProgressUpdate(String...strings) {
            System.out.println("imnotifying");
//            synchronized (Thread.currentThread()){
//                Thread.currentThread().notify();
            //}
            return;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
                String portNumber="11108";
            try {
                if(!msgs[0].contains("newnode")){
                    //System.out.println(msgs[0]);
                    portNumber = (Integer.parseInt(msgs[0].substring(0,4))*2)+"";
                    System.out.println("Port Number is "+portNumber);
                }

                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(portNumber));
                PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
                String msgToSend = msgs[0];
                if(msgs[0].contains("5554directthiskey")){
                    String delimeter = "sriramreddy";
                    msgToSend=msgs[0]+delimeter+msgs[1]+delimeter+msgs[2]+delimeter+msgs[3];
                    System.out.println("message"+msgToSend);
                }
                pw.write(msgToSend);
                pw.flush();
                pw.close();
                socket.close();
            }
            catch (UnknownHostException e) {
                Log.e("Unknown Host :"+e, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e("IOE :"+e, "ClientTask socket IOException");
            }
           return null;
        }
    }
}
