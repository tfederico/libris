package com.tfederico.libris.contract;

import java.io.IOException;

/**
 * Created by davide on 28/04/17.
 */
public interface ISearcher {

    ISearchResult search() throws IOException;
}
