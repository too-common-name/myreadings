package org.modular.playground.catalog.web.graphql;

import org.eclipse.microprofile.graphql.Input;
import org.modular.playground.catalog.web.dto.BookRequestDTO;

@Input
public class BookGql extends BookRequestDTO {}