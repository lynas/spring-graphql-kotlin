package com.lynas.graphqlks

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@SpringBootApplication
class GraphqlKsApplication {

    @Bean
    fun init(postService: PostService, authorService: AuthorService) = CommandLineRunner {
        val author1id = UUID.randomUUID().toString()
        val author2id = UUID.randomUUID().toString()
        val post1 = Post(UUID.randomUUID().toString(), "t1", "txt1", author1id)
        val post2 = Post(UUID.randomUUID().toString(), "t2", "txt2", author1id)
        val post3 = Post(UUID.randomUUID().toString(), "t3", "txt3", author2id)
        val post4 = Post(UUID.randomUUID().toString(), "t4", "txt4", author2id)
        val post5 = Post(UUID.randomUUID().toString(), "t5", "txt5", author2id)
        postService.saveAllPost(listOf(post1, post2, post3, post4, post5))
                .forEach { println(it) }

        val author1 = Author(author1id, "n1", "t1")
        val author2 = Author(author2id, "n2", "t2")

        authorService.saveAllAuthors(listOf(author1, author2))
                .forEach {
                    println(it)
                }
    }
}

fun main(args: Array<String>) {
    runApplication<GraphqlKsApplication>(*args)
}

@Entity
data class Author(@Id val id: String, val name: String, val thumbnail: String)

@Entity
data class Post(@Id val id: String, val text:String, val category: String, val authorId: String)

interface AuthorRepository : JpaRepository<Author, String>

interface PostRepository : JpaRepository<Post, String> {
    fun getByAuthorId(authorId: String): List<Post>
}

@Service
@Transactional
class AuthorService(private val authorRepository: AuthorRepository) {
    fun getAuthors() = authorRepository.findAll()

    fun saveAllAuthors(entities: Iterable<Author>) = authorRepository.saveAll(entities)

    fun getAuthor(id: String) = authorRepository.findById(id).orElse(null)

}


@Service
class PostService(val postRepository: PostRepository) {
    fun saveAllPost(entities: Iterable<Post>) = postRepository.saveAll(entities)

    fun getRecentPosts(count: Int, offset: Int) =  postRepository.findAll()

    fun getPostByAuthorId(authorId: String) = postRepository.getByAuthorId(authorId)
}


@Component
class Query(val postService: PostService, val authorService: AuthorService) : GraphQLQueryResolver {
    fun recentPosts(count: Int, offset: Int): List<Post> {
        return postService.getRecentPosts(1,1)
    }

    fun authors() = authorService.getAuthors()

    fun author(id: String) = authorService.getAuthor(id)

}