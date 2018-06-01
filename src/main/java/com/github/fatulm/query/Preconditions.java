package com.github.fatulm.query;

/**
 * Helper class for preconditions
 */
class Preconditions {
    public Preconditions() {
    }

    /**
     * Checks encoded characters for bad structure
     *
     * @throws IllegalArgumentException if encoded characters have bad structure
     */
    public static void checkEncodedCharacters() {
        // TODO: not complete + not tested
    }

    /**
     * Checks query string structure.
     * Key and value can be empty.
     * And each key can contain multiple values.
     * But your string can not have key=value1=value2,
     * instead use key=value1&amp;key=value2
     *
     * @param query query string which is being checked
     * @throws IllegalArgumentException when query has invalid structure
     */
    public static void checkStructure(String query) {
        // for each part: "([^=&]*=?[^=&]*)" (matches empty)
        // structure: "(part)(&(part))*"

        if (!query.matches("([^=&]*=?[^=&]*)(&([^=&]*=?[^=&]*))*"))
            throw new IllegalArgumentException("query string has bad structure");
    }

    /**
     * In addition to all alphanumerics and percent encoded characters,
     * a query can legally include the following unencoded characters:
     * / ? : @ - . _ ~ ! $ &amp; ' ( ) * + , ; =
     * This method lets having white space characters
     *
     * @param query query string which is being checked
     * @throws IllegalArgumentException when query has invalid characters
     */
    public static void checkCharactersGeneral(String query) {
        if (!query.matches("[\\w\\s.+*\\-%/?:@_~!$&(),;=']*"))
            throw new IllegalArgumentException("query string has invalid characters");

        // TODO: not complete + not tested completely
    }

    /**
     * This method ensures that a given query string does not have white space
     * When white space is not valid we use this method
     *
     * @param query query string which is being checked
     * @throws IllegalArgumentException when query has white space characters
     */
    public static void checkWhiteSpaceCharacters(String query) {
        if (!query.matches("[^\\s]*"))
            throw new IllegalArgumentException("query string contains unencoded white space");
    }

    /**
     * @throws NullPointerException if query is null
     */
    public static void checkQueryNonNull(String query) {
        if (query == null)
            throw new NullPointerException("query string should not be null");
    }
}
