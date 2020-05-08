# spring-graphql-kotlin

- Run application with following command

```
./gradlew bootRun
```

- Graphql endpoint
```
http://localhost:8080/graphql
```

- Sample graphql query

```
query {
    recentPosts(count: 1, offset: 1) {
        id
        text
    }
}

query {
    recentPosts(count: 1, offset: 1) {
        id
        text
        author{
            id
            name
        }
    }
}


query {
    authors {
        id
        name
        thumbnail
    }
}

query {
    authors {
        id
        name
        thumbnail
        posts{
            id
            text
        }
    }
}

```
