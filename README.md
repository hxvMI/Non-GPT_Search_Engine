# NotGPT Search Engine

NotGPT is a basic search engine implementation that indexes web pages and words, ranks pages using a variant of the PageRank algorithm, and performs searches based on indexed words. The search engine is built using Java and includes the following components:

- **`NotGPT` class**: Implements the `SearchEngine` interface, manages indexing and ranking of pages and words.
- **`InfoFile` class**: Represents information stored about a web page or word, including its index and influence score.
- **`HardDisk` class**: Simulates a disk for storing indexed pages and words.
- **`SearchEngine` interface**: Defines methods for collecting data, ranking pages, and performing searches.
- **`Browser` interface**: Provides methods to load pages and retrieve URLs and words from those pages.
- **`Rank` class**: Contains the main method to demonstrate ranking functionality.
- **`Search` class**: Contains the main method to demonstrate searching functionality.

## Dependencies

- **Java 8** or higher
- **No external libraries** are required


## Project Structure

```plaintext
prog11/
│
├── InfoFile.java
├── NotGPT.java
├── Rank.java
├── Search.java
├── SearchEngine.java
└── HardDisk.java
```

## Usage

### Indexing and Ranking

1. **Collect Data**: Use the `collect` method of the `NotGPT` class to collect and index data from web pages.

   ```java
   SearchEngine notGPT = new NotGPT();
   notGPT.collect(browser, startingURLs);
   ```

   - **`browser`**: An instance of a class implementing the Browser interface.
   - **`startingURLs`**: A list of starting URLs to collect data from.

2. **Rank Pages**: Use the rank method to assign priorities to pages based on their importance.

   ```java
   notGPT.rank(false); // Slow ranking
   notGPT.rank(true);  // Fast ranking
   ```

   - **`fast`**: A boolean indicating whether to use fast ranking (true) or slow ranking (false).

3. **Save and Load Data**: Data is saved and loaded from disk files. For example:

   ```java
   pageDisk.write("slow.txt");
   pageDisk.read("slow.txt");
   ```
   - **`pageDisk`**: An instance of the HardDisk class.
   - **`"slow.txt"`**: The filename used for saving or loading the data.

### Searching

1. **Perform Searches**: Use the search method to find pages containing specified words and return them in order of importance.

   ```java
   List<String> searchWords = Arrays.asList("word1", "word2");
   String[] results = notGPT.search(searchWords, 5);
   ```
   - **`searchWords`**: A list of words to search for.
   - **`numResults`**: The maximum number of results to return.
   - **`results`**: An array of page URLs or identifiers in order of decreasing importance.


## Implementation Details

### Overview

The `NotGPT` class implements the `SearchEngine` interface to provide a search engine with web page indexing and ranking capabilities. The core functionalities include collecting web pages, ranking them based on their importance, and performing search queries.

### Key Components

1. **`InfoFile` Class**
   - Represents information stored about a web page or word.
   - Contains fields for storing the URL or word, influence metrics, and indices of related pages or words.

2. **`NotGPT` Class**
   - Implements the `SearchEngine` interface.
   - Uses two `HardDisk` instances (`pageDisk` and `wordDisk`) to manage data storage for web pages and words.
   - Utilizes two maps (`urlToIndex` and `wordToIndex`) to keep track of the indices for URLs and words.

### Methods

#### `collect(Browser browser, List<String> startingURLs)`

- **Purpose**: Collects and indexes web pages starting from the given URLs.
- **Process**:
  - Initializes a queue for page indexing.
  - Iterates over starting URLs to index and enqueue new pages.
  - Loads each page using the `Browser` instance, indexes URLs and words found on the page, and updates their influence metrics.

#### `rank(boolean fast)`

- **Purpose**: Assigns priorities to web pages using the PageRank algorithm.
- **Parameters**:
  - `fast` (boolean): Determines whether to use fast ranking (`true`) or slow ranking (`false`).
- **Process**:
  - Initializes influence values for pages.
  - If `fast` is `false`, calls `rankSlow` method 20 times.
  - If `fast` is `true`, calls `rankFast` method 20 times.

#### `search(List<String> searchWords, int numResults)`

- **Purpose**: Searches for pages containing all specified search words and returns them in order of decreasing importance.
- **Parameters**:
  - `searchWords` (List<String>): A list of words to search for.
  - `numResults` (int): The maximum number of search results to return.
- **Returns**: An array of URLs of the top pages matching the search criteria.

### Ranking Algorithms

- **Slow Ranking (`rankSlow`)**:
  - Distributes influence among pages linked from a given page.
  - Repeats the ranking process 20 times to converge on a stable set of influence values.

- **Fast Ranking (`rankFast`)**:
  - Uses a list of votes to update influence values more efficiently.
  - Processes votes in a sorted manner to optimize the ranking computation.

### Data Management

- **Save and Load**:
  - Data is managed using the `HardDisk` class.
  - Pages and words are indexed and stored in disk files which can be read and written using `read` and `write` methods.






