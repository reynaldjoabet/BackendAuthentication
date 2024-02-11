# BackendAuthentication

## Role-Based Access Control
Role-based access control (RBAC) refers to the idea of assigning permissions to users based on their role within an organization. It offers a simple, manageable approach to access management that is less prone to error than assigning permissions to users individually.
When using RBAC for Role Management, you analyze the needs of your users and group them into roles based on common responsibilities. You then assign one or more roles to each user and one or more permissions to each role. The user-role and role-permissions relationships make it simple to perform user assignments since users no longer need to be managed individually, but instead have privileges that conform to the permissions assigned to their role(s).


Reification means transforming something abstract (e.g. side effects, accessing fields, structure) into something "real" (values).

In functional effects, we reify by turning side-effects into values. For example, we might have a simple statement like;

```scala
println("Hello")
println("World")
In ZIO we reify this statement to a value like
val effect1 = F(println("Hello"))
val effect2 = F(println("World"))
```

Optics provide a way to access the fields of a case class and nested structures. There are three main types of optics:

- Lens: A lens is a way to access a field of a case class.
- Prism: A prism is a way to access a field of a nested structure or a collection.
- Traversal: A traversal is a way to access all fields of a case class, nested structures or collections.



JWT Authentication is a stateless authentication method well-suited for APIs and microservices architectures.



you might have app.example.com for the SPA and api.example.com for the API backend

Allow requests from app.example.com (or whatever your SPA's domain is) in the CORS configuration.

Ensure that your DNS records are correctly configured to point the subdomains (app.example.com and api.example.com) to the appropriate servers or hosting environments. This typically involves creating A or CNAME records in your DNS provider's dashboard.



`sudo -i -u postgres` to switch to this user
`psql` to connect to Postgres with user
`dscl . list /Users` to list all users on mac

