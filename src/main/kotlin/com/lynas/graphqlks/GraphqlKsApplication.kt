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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@SpringBootApplication
class GraphqlKsApplication {

    @Bean
    fun init(postService: PostService, authorService: AuthorService) = CommandLineRunner {
        val author1id = ID
        val author2id = ID
        val post1 = Post(ID, "t1", "txt1", author1id)
        val post2 = Post(ID, "t2", "txt2", author1id)
        val post3 = Post(ID, "t3", "txt3", author2id)
        val post4 = Post(ID, "t4", "txt4", author2id)
        val post5 = Post(ID, "t5", "txt5", author2id)
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

val ID : String
    get() = UUID.randomUUID().toString()

@Entity
data class Author(@Id val id: String, val name: String, val thumbnail: String)

data class AuthorInput(val name: String, val thumbnail: String?)

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

    fun saveAuthor(author: Author) = authorRepository.save(author)

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


@Component
class Mutation(val authorService: AuthorService) : GraphQLMutationResolver {

    fun author(operation:String, authorInput: AuthorInput) : Author? {
        when (operation) {
            "insert" ->
                return authorService.saveAuthor(
                        Author(id = ID,
                                name = authorInput.name,
                                thumbnail = authorInput.thumbnail ?: ""))

            else -> return null
        }
    }

}

@Component
class PostResolver(private val authorRepository: AuthorRepository) : GraphQLResolver<Post> {
    fun author(post: Post) = authorRepository.findById(post.authorId)

}

@Component
class AuthorResolver(val postService: PostService) : GraphQLResolver<Author> {
    fun posts(author: Author) = postService.getPostByAuthorId(author.id)
}