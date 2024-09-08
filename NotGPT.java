package prog11;

import prog05.ArrayQueue;

import java.util.*;

public class NotGPT implements SearchEngine{

    //Are both maps so you can use .get .put and so on
    Map<String, Long> urlToIndex = new TreeMap<>();
    Map<String, Long> wordToIndex = new HashMap<>();
    HardDisk pageDisk = new HardDisk();
    HardDisk wordDisk = new HardDisk();

    @Override
    public void collect(Browser browser, List<String> startingURLs) {
        Queue pageQueue = new ArrayQueue(); //page index queue
        System.out.println("starting pages " + startingURLs);



        //check all starting urls
        for (String currUrl : startingURLs){
            if (urlToIndex.containsKey(currUrl) == false){//check if not indexed
                pageQueue.offer(indexPage(currUrl));//index currUrl using indexPage and store it in temp to put back in Queue
            }
        }



        //while queue not empty
        while (pageQueue.isEmpty() == false){//dequeue a page index and since it returns a value store it for later
            System.out.println("queue " + pageQueue);


            Map<String, Boolean> seenWordsMap = new HashMap<>(); //Set of seen
            Set<String> seenURLSet = new HashSet<>(); //Set of seen
            Long pageIndex = (Long) pageQueue.poll(); //Dequeue and store
            InfoFile pageInfoFile = pageDisk.get(pageIndex);
            String pageURL = pageInfoFile.data;//get its url from pageDisk using its index
            boolean loaded = browser.loadPage(pageURL);


            System.out.println("dequeued " + pageInfoFile);


            ///////////////////////////URL SECTION
            //Index each URL on page if not indexed yet and add it to page index indices list the first time its seen
            if (loaded == true){//if loaded get the list of its urls
                List<String> listOfUrl = browser.getURLs();//.getURLs returns a list
                System.out.println("urls " + listOfUrl);
                for (String currIndex : listOfUrl) {// go through each url
                    if (seenURLSet.contains(currIndex) == false) {// if not in seenSet
                        long urlIndex;
                        if (urlToIndex.containsKey(currIndex)){ //check if currIndex is in urlToIndex
                            urlIndex = urlToIndex.get(currIndex); //if yes retrieve the index
                        }
                        else {
                            urlIndex = indexPage(currIndex);//else index it and set urlIndex to it
                            pageQueue.offer(urlIndex);//put it in pageQueue
                        }

                        pageInfoFile.indices.add(urlIndex);//add to indices of page
                        seenURLSet.add(currIndex); //add to seen set
                    }
                }
                pageDisk.put(pageIndex, pageInfoFile); //update pageDisk and print statement for update
                System.out.println("updated page file " + pageInfoFile);






                ///////////////////////////WORDS SECTION
                //Index each word on page if not indexed yet and add it to page index indices list the first time its seen
                List<String> listOfWords = browser.getWords();//.getWords from browser
                System.out.println("words " + listOfWords);//print list of words
                for (String currIndex : listOfWords){//go through each word
                    if (seenWordsMap.containsKey(currIndex) == false){ //if not see do this
                        long wordIndex;
                        if (wordToIndex.containsKey(currIndex)){//check if wordToIndex has currIndex
                            wordIndex = wordToIndex.get(currIndex);//if yes set it to the index in there
                        }
                        else {
                            wordIndex = indexWord(currIndex);//if no index, index it using indexWord method
                        }


                        InfoFile wordInfoFile = wordDisk.get(wordIndex); //get infoFile of the wordIndex
                        wordInfoFile.indices.add(pageIndex); //add index to indices list of word
                        seenWordsMap.put(currIndex, true);//update seenMap
                        wordDisk.put(wordIndex, wordInfoFile);//update wordDisk         //has to be in forloop because uses thing from in here
                        System.out.println("updated word file " + wordInfoFile);//make a print statement to detail update
                        if (seenWordsMap.containsKey(currIndex) == false) seenWordsMap.put(currIndex, true); //check to make sure it updated seenMap
                    }
                }
            }
        }


    }

    @Override
    public void rank(boolean fast) {

        for (Map.Entry<Long,InfoFile> entry : pageDisk.entrySet()) {
            long pageIndex = entry.getKey();
            InfoFile infoFile = entry.getValue();
            infoFile.influence = 1.0; //set influence for each file to 1.0
            infoFile.influenceTemp = 0.0; //set temp influ to 0.0
        }

        int count = 0;//count of pages with no links
        for (Map.Entry<Long,InfoFile> entry : pageDisk.entrySet()) {
            long pageIndex = entry.getKey();
            InfoFile infoFile = entry.getValue();
            if (infoFile.indices.isEmpty())count++; //if indices is empty this means it has no links so increase count
        }

        double defaultInfluence = 1.0 * count / pageDisk.size();     //change double defaultInflu to current //after loop declare defaultInflu = 0.0


        if(fast == false){                               //if fast false call rankSlow(defInfu) 20 times
            for (int i = 0; i < 20; i++)
                rankSlow(defaultInfluence);
        }
        else if (fast == true) {                         //if fast true call rankFast(defInfu) 20 times
            for (int i = 0; i < 20; i++)
                rankFast(defaultInfluence);
        }

    }




    //HW 5.
    public class PageComparator implements Comparator<Long> {


        @Override
        public int compare(Long pageIndex1, Long pageIndex2) {
            return Double.compare(pageDisk.get(pageIndex1).influence, pageDisk.get(pageIndex2).influence);
        }
    }


    @Override
    public String[] search(List<String> searchWords, int numResults) {


        //HW 9. remove any words that have not been indexed from searchWords.  If searchWords ends up empty, return an empty array.  Test.
        Iterator<String> searchIterator = searchWords.iterator();
        while (searchIterator.hasNext()){
            if (!wordToIndex.containsKey(searchIterator.next())){
                searchIterator.remove();
            }}
        if (searchWords.isEmpty())return new String[0];



        //1.
        // Iterator into list of page indices for each key word
        // Current page index in each list, just ``behind'' the iterator.
        PriorityQueue<Long> bestPageIndices = new PriorityQueue<>(new PageComparator());
        Iterator<Long>[] wordPageIndexIterators = (Iterator<Long>[]) new Iterator[searchWords.size()];
        long[] currentPageIndex;
        int count = 0;

        for (String currWord : searchWords){
            InfoFile infoFile = wordDisk.get(wordToIndex.get(currWord));
            List<Long> indices = infoFile.indices;
            wordPageIndexIterators[count] = indices.iterator();
            count++;
        }

        currentPageIndex = new long[searchWords.size()];//Initialize currentPageIndex






        //4.
        //While getNextPageIndices is true check if the entries of currentPageIndex are all equal.
        //If so, you have a found a match.  Print out its URL
        while (getNextPageIndices(currentPageIndex, wordPageIndexIterators)){
            if (allEqual(currentPageIndex)){
                InfoFile infoFile = pageDisk.get(currentPageIndex[0]);
                System.out.println(infoFile.data);


                //HW 6.
                if (bestPageIndices.size() < numResults){//If the priority queue is not "full" (has numResults elements), just offer the matching page index.
                    bestPageIndices.offer(currentPageIndex[0]);
                }
                else if (bestPageIndices.size() == numResults) {//If the priority queue is full, use peek() and pageComparator to determine if matching page should go into the queue.
                    double temp = pageDisk.get(bestPageIndices.peek()).influence;
                    if (infoFile.influence > temp){
                        bestPageIndices.poll();
                        bestPageIndices.offer(currentPageIndex[0]);
                    }
                }
            }
        }


        ////HW 7. Temporary
        String[] resultsQueue = new String[bestPageIndices.size()];
        //while bestpages isnt empty start at the end and decrease
        for (int fromBack = resultsQueue.length - 1; !bestPageIndices.isEmpty(); fromBack--) {
            String currWord = pageDisk.get(bestPageIndices.poll()).data;
            resultsQueue[fromBack] = currWord;
        }



        return resultsQueue;
    }







    //2.
    /** Check if all elements in an array of long are equal.
     @param array an array of numbers
     @return true if all are equal, false otherwise
     */
    private boolean allEqual(long[] array) {
        ///Change this is it doesn't work
        if (array.length <= 1)return true; //if there are no elements or just 1 that means there is no need to compare

        long currEle = array[0];
        for (int i = 1; i < array.length; i++){
            if (array[i] != currEle)return false;
        }
        return true;
    }

    /** Get the largest element of an array of long.
     @param array an array of numbers
     @return largest element
     */
    private long getLargest (long[] array) {
        long largest = array[0];
        for (int i = 1; i < array.length; i++){
            if (array[i] > largest)largest = array[i];
        }
        return largest;
    }

    //3.
    /** If all the elements of currentPageIndex are equal,
     set each one to the next() of its Iterator,
     but if any Iterator hasNext() is false, just return false.

     Otherwise, do that for every element not equal to the largest element.

     Return true.

     @param currentPageIndex array of current page indices
     @param wordPageIndexIterators array of iterators with next page indices
     @return true if all page indices are updated, false otherwise
     */
    private boolean getNextPageIndices (long[] currentPageIndex, Iterator<Long>[] wordPageIndexIterators) {

        if (allEqual(currentPageIndex)){
            for(int i = 0; i < currentPageIndex.length; i++){
                if (wordPageIndexIterators[i].hasNext()){
                    currentPageIndex[i] = wordPageIndexIterators[i].next();
                }
                else return false;
            }
        }
        else {
            long largestEle = getLargest(currentPageIndex);
            for (int i = 0; i < currentPageIndex.length; i++) {
                if (currentPageIndex[i] != largestEle){
                    if (!wordPageIndexIterators[i].hasNext()){
                        return false;
                    }
                    else {
                        currentPageIndex[i] = wordPageIndexIterators[i].next();
                    }
                }
            }
        }
        return true;
    }

    public long indexPage(String url){

        long index = pageDisk.newFile();// .newFile returns an index
        InfoFile infoFile = new InfoFile(url);//takes a String


        //index comes from pageDisk.newFile
        //store infoFile in pageDisk
        pageDisk.put(index,infoFile);
        urlToIndex.put(url,index); //map url to index

        //Put a print statement inside your indexPage method.
        //use + url if you don't want the [] at the end
        System.out.println("indexing page " + index + " " + infoFile);



        return index; //return index

    }

    //Basically the same as indexPage just for words instead
    public long indexWord(String word) {
        long index = wordDisk.newFile();// .newFile returns an index
        InfoFile infoFile = new InfoFile(word);//takes a String


        //index comes from pageDisk.newFile
        //store infoFile in pageDisk
        wordDisk.put(index,infoFile);
        wordToIndex.put(word,index); //map url to index

        //Put a print statement inside your indexPage method.
        //use + url if you don't want the [] at the end
        System.out.println("indexing word " + index + " " + infoFile);


        return index; //return index
    }


    void rankSlow (double defaultInfluence){

        for (Map.Entry<Long,InfoFile> entry : pageDisk.entrySet()) {
            long pageIndex = entry.getKey();
            InfoFile infoFile = entry.getValue();       // per is the same as saying divided by so it would be influence/#ofindex's
            double influencePerIndex = infoFile.influence/infoFile.indices.size();             //initialize influencePerIndex



            for (long index : infoFile.indices){                 //for each index
                InfoFile pageFile = pageDisk.get(index);
                pageFile.influenceTemp = pageFile.influenceTemp + influencePerIndex;    //add influPerIndex to the influenceTemp of the page with that index
            }
        }
        for (Map.Entry<Long,InfoFile> entry : pageDisk.entrySet()) {            //visit each page file again
            long pageIndex = entry.getKey();
            InfoFile infoFile = entry.getValue();

            infoFile.influence = infoFile.influenceTemp + defaultInfluence;             //Set influence to InfluTemp plus defaultInflu
            infoFile.influenceTemp = 0.0;                                           //Set its influenceTemp to 0.0
        }

    }

    //////////////FIX LATER
        void rankFast (double defaultInfluence){
            int count = 0;//initalize count to track list check amounts
            List<Vote> votesList = new ArrayList<>();//make voteslist

            for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()){
                long pageIndex = entry.getKey();
                InfoFile infoFile = entry.getValue();
                for (long index : infoFile.indices){//add votes
                    double voteValue = infoFile.influence / infoFile.indices.size();
                    votesList.add(new Vote(index, voteValue));
                }
            }

            Collections.sort(votesList);//sort
            Iterator<Vote> voteIterator = votesList.iterator();
            Vote vote = null;
            if (voteIterator.hasNext()){//if has next
                vote = voteIterator.next();//set vote value
            }


            for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()){
                long pageIndex = entry.getKey();
                InfoFile infoFile = entry.getValue();

                while (vote != null){//iterate through votelist until the end is reached by either pageIndex or count
                    boolean endLoop = false;

                    if (pageIndex < vote.index){//set new infoFIle values since the end of the list // conditional
                        endLoop = true;         //if at end of loop then set endloop to true
                    }
                    else if (pageIndex == vote.index){
                        infoFile.influenceTemp = infoFile.influenceTemp + vote.vote;
                        if (++count < votesList.size()){//if less then move up iterator
                            vote = voteIterator.next();
                        }
                        else{//set new infoFIle values since the end of the list
                            endLoop = true;       //if at end of loop then set endloop to true
                        }
                    }
                    if (endLoop == true){//update infoFiles then break out of loop
                        infoFile.influence = infoFile.influenceTemp + defaultInfluence;
                        infoFile.influenceTemp = 0.0;
                        break;
                    }
                }
            }
        }



    public class Vote implements Comparable<Vote>{
        double vote;
        Long index;

        public Vote(Long index, double vote){
            this.index = index;
            this.vote = vote;
        }


        @Override
        public int compareTo(Vote temp) {
            int indexComp = Long.compare(this.index, temp.index);

            if (indexComp != 0)return indexComp; //compareTo method should return the result of comparing the indexes if they are unequal.
            else return Double.compare(this.vote, temp.vote); //else return the result

        }
    }




}
