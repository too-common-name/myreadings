package modules.review.web.controllers;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import modules.catalog.core.domain.Book;
import modules.catalog.core.usecases.BookService;
import modules.review.core.domain.Review;
import modules.review.core.domain.ReviewStats;
import modules.review.core.usecases.ReviewService;
import modules.review.web.dto.ReviewRequestDTO;
import modules.review.web.dto.ReviewResponseDTO;
import modules.review.web.dto.ReviewStatsResponseDTO;
import modules.user.core.domain.User;
import modules.user.core.usecases.UserService;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.Authenticated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/v1/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ReviewController {

    @Inject
    ReviewService reviewService;

    @Inject
    BookService bookService;

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    @Context
    SecurityContext ctx;

    private UUID getCurrentUserIdFromJwt() {
        String userIdClaim = jwt.getClaim("sub");
        if (userIdClaim == null) {
            throw new NotAuthorizedException("User ID not found in JWT.");
        }
        return UUID.fromString(userIdClaim);
    }

    private ReviewResponseDTO mapToReviewResponseDTO(Review review) {
        return ReviewResponseDTO.builder()
                .reviewId(review.getReviewId())
                .bookId(review.getBook().getBookId())
                .userId(review.getUser().getKeycloakUserId())
                .reviewText(review.getReviewText())
                .rating(review.getRating())
                .publicationDate(review.getPublicationDate())
                .username(review.getUser().getUsername())
                .build();
    }

    private Review findReviewAndCheckOwnership(UUID reviewId, UUID currentUserId) {
        Optional<Review> existingReviewOptional = reviewService.findReviewById(reviewId);
        if (existingReviewOptional.isEmpty()) {
            throw new NotFoundException("Review not found with ID: " + reviewId);
        }
        Review existingReview = existingReviewOptional.get();
        if (!existingReview.getUser().getKeycloakUserId().equals(currentUserId)) {
            throw new ForbiddenException("You are not authorized to access this review.");
        }
        return existingReview;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "user", "admin" })
    public Response createReview(@Valid ReviewRequestDTO createReviewRequestDTO) {
        try {
            UUID userId = getCurrentUserIdFromJwt();

            Optional<Book> book = bookService.getBookById(createReviewRequestDTO.getBookId());
            Optional<User> user = userService.findUserProfileById(userId);

            if (book.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Book not found.").build();
            }
            if (user.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("User not found.").build();
            }

            Review reviewToCreate = Review.builder()
                    .reviewId(UUID.randomUUID())
                    .book(book.get())
                    .user(user.get())
                    .reviewText(createReviewRequestDTO.getReviewText())
                    .rating(createReviewRequestDTO.getRating())
                    .publicationDate(LocalDateTime.now())
                    .build();

            Review createdReview = reviewService.createReview(reviewToCreate);

            return Response.status(Response.Status.CREATED)
                    .entity(mapToReviewResponseDTO(createdReview))
                    .build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{reviewId}")
    @RolesAllowed({ "user", "admin" })
    public Response getReviewById(@PathParam("reviewId") UUID reviewId) {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();
            return Response.ok(mapToReviewResponseDTO(findReviewAndCheckOwnership(reviewId, currentUserId))).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{reviewId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "user", "admin" })
    public Response updateReview(@PathParam("reviewId") UUID reviewId, @Valid ReviewRequestDTO updateReviewRequestDTO) {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();

            Review existingReview = findReviewAndCheckOwnership(reviewId, currentUserId);

            Optional<Book> book = bookService.getBookById(updateReviewRequestDTO.getBookId());
            Optional<User> user = userService.findUserProfileById(currentUserId);

            if (book.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Book not found.").build();
            }
            if (user.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("User not found.").build();
            }

            Review reviewToUpdate = Review.builder()
                    .reviewId(reviewId)
                    .book(existingReview.getBook())
                    .user(existingReview.getUser())
                    .reviewText(updateReviewRequestDTO.getReviewText())
                    .rating(updateReviewRequestDTO.getRating())
                    .publicationDate(existingReview.getPublicationDate())
                    .build();

            Review updatedReview = reviewService.updateReview(reviewToUpdate);

            return Response.ok(mapToReviewResponseDTO(updatedReview)).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{reviewId}")
    @RolesAllowed({ "user", "admin" })
    public Response deleteReviewById(@PathParam("reviewId") UUID reviewId) {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();

            findReviewAndCheckOwnership(reviewId, currentUserId);
            reviewService.deleteReviewById(reviewId);
            return Response.noContent().build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/books/{bookId}")
    @RolesAllowed({ "user", "admin" })
    public Response getReviewsByBookId(@PathParam("bookId") UUID bookId) {
        List<Review> reviews = reviewService.getReviewsForBook(bookId);
        return Response.ok(reviews.stream()
                .map(this::mapToReviewResponseDTO)
                .collect(Collectors.toList())).build();
    }

    @GET
    @Path("/books/{bookId}/stats")
    @RolesAllowed({ "user", "admin" })
    public Response getBookReviewStats(@PathParam("bookId") UUID bookId) {
        try {
            ReviewStats stats = reviewService.getReviewStatsForBook(bookId);

            ReviewStatsResponseDTO responseDTO = ReviewStatsResponseDTO.builder()
                    .bookId(bookId.toString())
                    .totalReviews(stats.getTotalReviews())
                    .averageRating(stats.getAverageRating())
                    .build();

            return Response.ok(responseDTO).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }    

    @GET
    @Path("/users/{userId}")
    @RolesAllowed({ "user", "admin" })
    public Response getReviewsByUserId(@PathParam("userId") UUID userId) {
        try {
            UUID currentUserId = getCurrentUserIdFromJwt();

            if (!userId.equals(currentUserId) && !ctx.isUserInRole("admin")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("You are not authorized to view reviews for this user.").build();
            }

            List<Review> reviews = reviewService.getReviewsForUser(userId);
            return Response.ok(reviews.stream()
                    .map(this::mapToReviewResponseDTO)
                    .collect(Collectors.toList())).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }
}