package it.polpetta.libris;

/**
 * Created by davide on 28/04/17.
 */
public interface IAbstractFactoryReverseSearchProvider {

    // I need a query builder here
    public IQueryBuilder imageSearchBuildQuery();

}
