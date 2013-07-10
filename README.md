# thundr-rest

*thundr-rest* is a [thundr](http://3wks.github.io/thundr/) module which makes creating RESTful endpoints within a
thundr app relatively painless. thundr-rest neatly separates the data (i.e. your object) being returned from the
serialization format (e.g. JSON, XML etc.).

## Quick start

Add the thundr-rest module to your module.properties. Be sure to add it _after_ your application module like so:

```
myapp=
com.threewks.thundr.rest=
```

Then annotate your controller method with `@Rest` and make sure it returns a `RestView` like so:

```java
class MyController {
  @Rest
  public RestView get(Integer id) {
    MyModel model = MyModelRepository.get(id);
    return new RestView(model);
  }
}
```

From here configure your routes as normal.

### Serialization formats
Both JSON and XML serialization formats are baked in. The default format when none is specified is JSON.

An example of changing the format via the `GET` parameter `format`:

`curl http://localhost:8080/myapp/mymodel?format=xml`

Formats can also be chosen by including an `Accept` header in the request with a recognised content type:

```
GET /myapp/mymodel HTTP/1.1
Host: localhost:8080
Accept: application/xml
```

**Note**: JSONP is also supported when the format is `json` by including the `callback` parameter.

## Why use it?

Lets say you've been given the task of creating up a RESTful API to fetch model objects from your repository by ID. Your
endpoint must support JSON and XML formats. In your thundr controllers you would ordinarily return specific view types
such as the following:

```java
class MyController {
  public JsonView get(Integer id) {
    MyModel model = MyModelRepository.get(id);
    return new JsonView(model);
  }
}
```

The obvious issue here is that in order to supply another serialization format (e.g. XML) you must add another method
which returns a different `View` type.

Another potential issue is parameterising the serialization format within a request. In the model above the only real
solution is to create separate routes using unique extensions such as .json or .xml to specify the desired format. For
example:

*MyController.java*

```java
class MyController {
  public JsonView getJson(Integer id) {
    MyModel model = MyModelRepository.get(id);
    return new JsonView(model);
  }

  public XmlView getXml(Integer id) {
    MyModel model = MyModelRepository.get(id);
    return new XmlView(model);
  }
}
```

*routes.json*

```json
{
  "/mymodel/{id}.json": { "GET": "myapp.controller.MyController.getJson",
  "/mymodel/{id}.xml": { "GET": "myapp.controller.MyController.getXml"
}
```

This isn't very DRY and also has some shortcomings from a REST standpoint such as:
1. It doesn't support selecting format via Accept headers.
2. Requires checking for a callback parameter on every JSON controller method if you wish to support JSONP.
3. Exceptions thrown will be rendered using the container's default error templates.

## How it works

thundr-rest uses main two constructs to address the shortcomings above:
1. The `@Rest` annotation, and;
2. The `RestView` controller return type.

### The @Rest annotation

The `@Rest` annotation applies the `RestActionInterceptor` to the controller method. Among other things
`ActionInterceptor`s have the ability to catch exceptions thrown in controller methods and do something with them other
than simply letting them bubble up to the container's top level exception handler.

In our case the `RestActionInterceptor` catches exceptions and wraps them in a `RestView` which will be serialised
according to the requested format.

### The RestView

The `RestView` return type conveys no information about how the data should be serialized. You simply set your output
object and optionally a HTTP status code and a character encoding and that's all. For example:

```java
new RestView(myData, HttpServletResponse.SC_CREATED, "UTF-8")
```

The `RestViewResolver` (which is automatically registered in the thundr injection context when you include the module)
then handles all the details about how the data should be serialized and returned to the user.

## Configuring additonal serializers

In the event that you wish to support additional serialization formats it's easy to add your own. Say you wanted to make
your endpoints browsable using HTML. In your application `InjectionConfiguration#configure` method add the following:

```java
// 1. Get a reference to the view resolver registry from the injectionContext
ViewResolverRegistry viewResolverRegistry = injectionContext.get(ViewResolverRegistry.class);
// 2. Find the RestViewResolver inside the registry by passing a dummy RestView
RestViewResolver restViewResolver = (RestViewResolver) viewResolverRegistry.findViewResolver(new RestView(null));
// 3. Add our custom serializer where HtmlSerializer is a custom class that implements com.threewks.thundr.rest.serializer.Serializer
restViewResolver.addSerializer("text/html", new HtmlSerializer());
```

Now when the `Accept` header contains "text/html" or the `format` parameter is "html" your custom serializer will be
used.

--------------
thundr-rest - Copyright (C) 2013 3wks