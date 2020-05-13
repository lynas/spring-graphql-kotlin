package com.lynas.graphqlks

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

val ID: String
    get() = UUID.randomUUID().toString()

@SpringBootApplication
class GraphqlKsApplication {

    @Bean
    fun init(postRepository: PostRepository, authorRepository: AuthorRepository) = CommandLineRunner {
        val author1id = ID
        val author2id = ID
        val post1 = Post(ID, "t1", author1id)
        val post2 = Post(ID, "t2", author1id)
        val post3 = Post(ID, "t3", author2id)
        val post4 = Post(ID, "t4", author2id)
        val post5 = Post(ID, "t5", author2id)

        postRepository.saveAll(listOf(post1, post2, post3, post4, post5)).forEach { println(it) }

        val author1 = Author(author1id, "n1")
        val author2 = Author(author2id, "n2")

        authorRepository.saveAll(listOf(author1, author2)).forEach { println(it) }
    }
}

fun main(args: Array<String>) {
    runApplication<GraphqlKsApplication>(*args)
}


@Entity
data class Author(@Id val id: String, val name: String)

data class AuthorInput(val name: String)

@Entity
data class Post(@Id val id: String, val text: String, val authorId: String)

interface AuthorRepository : JpaRepository<Author, String>

interface PostRepository : JpaRepository<Post, String> {
    fun getByAuthorId(authorId: String): List<Post>
}

@Component
class Query(val postRepository: PostRepository, val authorRepository: AuthorRepository) : GraphQLQueryResolver {

    fun posts() = postRepository.findAll()

    fun authors() = authorRepository.findAll()

    fun author(id: String) = authorRepository.findById(id)

}


@Component
class Mutation(val authorRepository: AuthorRepository) : GraphQLMutationResolver {

    fun author(operation: String, authorInput: AuthorInput): Author? {
        return when (operation) {
            "insert" -> authorRepository.save(Author(id = ID, name = authorInput.name))
            else -> null
        }
    }

}

@Component
class PostResolver(private val authorRepository: AuthorRepository) : GraphQLResolver<Post> {
    fun author(post: Post) = authorRepository.findById(post.authorId)

}

@Component
class AuthorResolver(val postRepository: PostRepository) : GraphQLResolver<Author> {
    fun posts(author: Author) = postRepository.getByAuthorId(author.id)
}