# myreadings

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/myreadings-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

# Domain Description: Book Reading App

This document provides a detailed description of the domain for the "Book Reading App," focusing on the core functionalities intended for the demo.

## Domain Overview

The Book Reading App domain revolves around the user's reading experience and their interactions with books.  It focuses on enabling users to discover books, manage their reading lists, and share (simplified) reviews. The core functionalities are streamlined for a demo, emphasizing key architectural aspects like user authentication via Keycloak and book data integration from Google Books APIs.

## Key Concepts

*   **Reading:** The central activity. The app aims to support and enhance the user's book reading journey.
*   **Books:** The fundamental domain object. Books are primarily identified and sourced from the external Google Books API catalog but are also represented internally within the system for specific application features.
*   **Users (Readers):** The primary actors in the system. Users register, search for books, create reading lists, write simplified reviews, and interact in a limited social manner (for the demo).
*   **Reading Lists:** Personal collections of books created by users to organize their reading activities. For the demo, we concentrate on the "Want to Read" list.
*   **Reviews:** Opinions and ratings written by users about books, shared with the community (in a simplified form for the demo).

## Entities and Attributes

This section details the entities within the domain and their attributes, mirroring the database schemas previously defined.

### 1. User ( `user_schema.user_profile` )

*   **Identity Management (External - Keycloak):** User identities (credentials, username, email) are managed by Keycloak. Our system references users through their `keycloak_user_id` (UUID).
*   **Entity Name in Database:** `user_profile` within the `user_schema` schema.
*   **Attributes:**
    *   `user_profile_id` (UUID, Primary Key): Unique internal identifier for the user profile within our application.
    *   `keycloak_user_id` (UUID, Unique, Not Null): UUID of the corresponding user in Keycloak. **Key link to the external authentication system.** Ensures a 1:1 relationship with Keycloak users.
    *   `profile_description` (Text, Optional): A brief description of the user profile (e.g., "Avid reader of science fiction and fantasy").
    *   `preferences_theme` (VARCHAR(50), Default 'light', Optional): Preferred application theme (e.g., 'light', 'dark').
    *   `avatar_url` (VARCHAR(2048), Optional): URL for the user's avatar image.
    *   `created_at` (Timestamp with Time Zone): Date and time of user profile creation.
    *   `updated_at` (Timestamp with Time Zone): Date and time of the last user profile update.

### 2. Book ( `catalog_schema.book` )

*   **External Identity (Google Books Volume ID):** Books are primarily identified by their `google_books_volume_id`.
*   **Entity Name in Database:** `book` within the `catalog_schema` schema.
*   **Attributes:**
    *   `book_id` (UUID, Primary Key): Unique internal identifier for the book within our system.
    *   `google_books_volume_id` (VARCHAR(255), Unique, Not Null): Unique `volumeId` assigned by Google Books. **Key link to the external Google Books catalog.**
    *   `isbn_13` (VARCHAR(20), Optional): ISBN-13 of the book (if available from Google Books).
    *   `title` (VARCHAR(255), Not Null): Title of the book.
    *   `author` (VARCHAR(255), Optional): Author of the book.
    *   `publication_date` (Date, Optional): Publication date of the book.
    *   `image_url` (VARCHAR(2048), Optional): URL of the book cover image (thumbnail from Google Books).
    *   `created_at` (Timestamp with Time Zone): Date and time when the book record was created in our system.
    *   `updated_at` (Timestamp with Time Zone): Date and time of the last update to the book record.

### 3. Reading List Entry ("Want to Read" - `readinglist_schema.reading_list_entry` )

*   **Entity Name in Database:** `reading_list_entry` within the `readinglist_schema` schema.
*   **Attributes:**
    *   `reading_list_entry_id` (UUID, Primary Key): Unique identifier for each entry in the reading list.
    *   `keycloak_user_id` (UUID, Not Null): Logical Foreign Key to the Keycloak user to whom this reading list entry belongs. **Links the list entry to a user.**
    *   `book_id` (UUID, Not Null): Foreign Key to `catalog_schema.book.book_id`. **Links the list entry to a book.**
    *   `start_date` (Date, Optional): Date when the user started reading the book (if tracked).
    *   `completion_date` (Date, Optional): Date when the user completed reading the book (if tracked).
    *   `status` (VARCHAR(50), limited values: 'to-read'): Status of the reading list entry. For the demo, we focus on 'to-read' (Want to Read). 'reading' and 'completed' statuses could be added later.
    *   `personal_rating` (Integer, Optional, Values 1-5): User's personal rating for the book *in this reading list* (different from public reviews). Useful for personal list organization.
    *   `notes` (Text, Optional): Personal notes from the user about the book in the list.
    *   `created_at` (Timestamp with Time Zone): Date and time of reading list entry creation.
    *   `updated_at` (Timestamp with Time Zone): Date and time of the last reading list entry update.
    *   **Unique Constraint (Composite `UNIQUE (keycloak_user_id, book_id)`):** Ensures a user can have a specific book in their "Want to Read" list at most once, preventing duplicates.

### 4. Review ( `review_schema.review` )

*   **Entity Name in Database:** `review` within the `review_schema` schema.
*   **Attributes:**
    *   `review_id` (UUID, Primary Key): Unique identifier for the review.
    *   `keycloak_user_id` (UUID, Not Null): Logical Foreign Key to the Keycloak user who wrote the review. **Links the review to a user.**
    *   `book_id` (UUID, Not Null): Foreign Key to `catalog_schema.book.book_id`. **Links the review to a book.**
    *   `rating_value` (Integer, Not Null, Values 1-5): Rating value for the review (mandatory, 1-5 stars).
    *   `comment_text` (Text, Not Null): Text of the review (mandatory).
    *   `review_date` (Timestamp with Time Zone): Date and time when the review was written.
    *   `likes_count` (Integer, Default 0): Number of "likes" received by the review (simplified for demo).
    *   `replies_count` (Integer, Default 0): Number of replies to the review (simplified for demo).
    *   `created_at` (Timestamp with Time Zone): Date and time of review creation.
    *   `updated_at` (Timestamp with Time Zone): Date and time of the last review update.

## Relationships between Entities (Summary)

*   **User (Keycloak User) ↔ User Profile (`user_schema.user_profile`)**: One-to-One relationship. Linked by `keycloak_user_id`.
*   **User (Keycloak User) ↔ Reading List Entry (`readinglist_schema.reading_list_entry`)**: One-to-Many relationship. Linked by `keycloak_user_id`.
*   **User (Keycloak User) ↔ Review (`review_schema.review`)**: One-to-Many relationship. Linked by `keycloak_user_id`.
*   **Book (`catalog_schema.book`) ↔ Reading List Entry (`readinglist_schema.reading_list_entry`)**: One-to-Many relationship. Linked by `book_id`.
*   **Book (`catalog_schema.book`) ↔ Review (`review_schema.review`)**: One-to-Many relationship. Linked by `book_id`.

## User Interaction Flows (Simplified for Demo Features)

1.  **User Registration:**
    *   User interacts with the registration interface (frontend).
    *   Frontend sends registration request to backend (User Module - `UserResource`).
    *   `UserService` (User Module) uses Keycloak Admin API to create the user in Keycloak.
    *   *(Optional)* `UserService` creates a `user_profile` record in the local database (`user_schema`) for application-specific data.
    *   Backend returns success (or error) response.

2.  **User Login:**
    *   User enters credentials into the login form (frontend).
    *   Frontend sends login request to Keycloak (directly or via backend intermediary, depending on desired demo flow).
    *   Keycloak authenticates user and returns a JWT token.
    *   Frontend (or backend) receives the JWT token for subsequent authenticated API requests.

3.  **Book Search:**
    *   User enters a search query into the search bar (frontend).
    *   Frontend sends search request to backend (Catalog Module - `CatalogResource`).
    *   `CatalogService` (Catalog Module) calls Google Books APIs with the search query.
    *   Google Books APIs return a list of books.
    *   `CatalogService` maps Google Books results to DTOs and returns them to `CatalogResource`.
    *   `CatalogResource` returns results to the frontend for display.

4.  **View Book Details:**
    *   User clicks on a book in the search results list (frontend).
    *   Frontend requests book details from backend (Catalog Module - `CatalogResource`), passing the book identifier (`volumeId` from Google Books).
    *   `CatalogService` (Catalog Module) retrieves book details from Google Books API (or cache if implemented).
    *   `CatalogService` *ensures* essential book information is stored in the local database (`catalog_schema.book`) using `ensureBookExists()` logic.
    *   `CatalogService` returns book details (from Google Books and/or local DB) to `CatalogResource`.
    *   `CatalogResource` returns details to the frontend for display in the book detail page.

5.  **Add Book to "Want to Read" List:**
    *   Authenticated user, on a book details page, clicks "Add to 'Want to Read'" button (frontend).
    *   Frontend sends a request to the backend (Reading List Module - `ReadingListResource`), including the book identifier (`volumeId` or internal `bookId`) and the user's JWT token.
    *   `ReadingListService` (Reading List Module) delegates to `CatalogService` to ensure the book's essential information is present in the local catalog database.
    *   `ReadingListService` creates a `reading_list_entry` record in the `readinglist_schema.reading_list_entry` table, linking the user, book, and "to-read" status.
    *   Backend returns success response.

6.  **View "Want to Read" List:**
    *   Authenticated user navigates to their profile or reading list section (frontend).
    *   Frontend sends a request to the backend (Reading List Module - `ReadingListResource`), including the user's JWT token.
    *   `ReadingListService` retrieves the user's "Want to Read" list from `readinglist_schema.reading_list_entry`, joining with `catalog_schema.book` to get book details.
    *   `ReadingListResource` returns the list of books (essential details) to the frontend.
    *   Frontend displays the user's "Want to Read" list.

7.  **Write a Review:**
    *   Authenticated user, on a book details page, interacts with a simplified review form (frontend).
    *   Frontend sends a request to the backend (Review Module - `ReviewResource`), including the book identifier, rating, review text, and the user's JWT token.
    *   `ReviewService` (Review Module) verifies the book exists in the local catalog (using `CatalogService` or directly querying `catalog_schema.book`), or ensures it's created if missing (using `CatalogService.ensureBookExists()`).
    *   `ReviewService` creates a `review` record in the `review_schema.review` table, linking the user, book, rating, and review text.
    *   Backend returns success response.

8.  **View Reviews for a Book:**
    *   User views the details page of a book (frontend).
    *   Frontend requests reviews for the book from the backend (Review Module - `ReviewResource`), passing the book identifier.
    *   `ReviewService` retrieves reviews for the book from `review_schema.review`, joining with `user_schema.user_profile` (or Keycloak directly, for username) to get reviewer information.
    *   `ReviewResource` returns the list of reviews to the frontend.
    *   Frontend displays the reviews on the book details page.

This detailed domain description should provide a solid foundation for moving into the implementation phase of your "Book Reading App" demo. Let me know if you have any specific areas you'd like to elaborate on or any further questions!