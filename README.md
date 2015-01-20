# peapod <a href="https://bayofmany.ci.cloudbees.com/"><img src="https://www.cloudbees.com/sites/default/files/styles/large/public/Button-Built-on-CB-1.png" width="150" align="right"></a>

> The project's name refers to the java class encapsulation of graph vertices. <br />
> It's a just like peapods wrapping peas.

An Object-Graph Wrapper (OGW) for the Tinkerpop 3 graph stack. This project has been created as an alternative to the Frames module in Tinkerpop 2.


This project is similar to the [Totorom](https://github.com/BrynCooke/totorom) library created by Bryn Cook and the [Ferma](https://github.com/Syncleus/Ferma) library created by Jeffrey Phillips Freeman. The main differences are:
* Peapod is based upon Tinkerpop 3.
* Like Tinkerpop Frames, Peapod uses code generation to implement the framed vertex and edge classes. It makes the integration with Tinkerpop 3 easier and minimizes code duplication. Unlike Tinkerpop Frames and Ferma, the code is generated at compile-time using  annotation processors. The generated code is completely transparent and readable by the developer. There is no startup cost of runtime code generation and the use of reflection is limited in order to have minimal performance overhead.

To integrate peapod in your project, include the following dependency. (Still working towards a first working release).

    <dependency>
        <groupId>org.bayofmany.peapod</groupId>
        <artifactId>peapod-core</artifactId>
        <version>0.1.0-SNAPSHOT</version>
    </dependency>

<img src="http://www.tinkerpop.com/docs/3.0.0.M6/images/tinkerpop-classic.png" width="400" >

This way you define the framed vertices and edges:

    @Vertex
    public abstract class Person {
      public abstract String getName();
      public abstract void setName(String name);

      public abstract List<Knows> getKnows();
      public abstract Knows getKnows(Person person);
      public abstract Knows addKnows(Person person);
      public abstract Knows removeKnows(Person person);
    }
    
    @Edge
    public abstract class Knows {
      public abstract void setYears(int years);
      public abstract int getYears();
    }

    
And this way you query for and interact with the framed objects:
    
    public void testClassic() {
        Graph g = TinkerFactory.createClassic();
        FramedGraph graph = new FramedGraph(g);
        Person marko = graph.v(1, Person.class);
        assertEquals("marko", marko.getName());

        List<Person> result = graph.V(Person.class).has("name", "josh").toList();
        assertEquals(1, result.size());
        assertEquals("josh", result.get(0).getName());
    }
    
This project uses code derived from the [Tinkerpop](http://www.tinkerpop.com/) project under the Apache license and/or Tinkerpop license.
