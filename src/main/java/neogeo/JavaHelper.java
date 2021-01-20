package neogeo;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;

import java.util.function.Function;

public class JavaHelper {

  @Context
  public GraphDatabaseService db;

  @Context
  public Log log;

//  protected static Function<Node, GeoNode> ResultFn = GeoNode::new;

  public static class GeoNode {
    public Node node;

    public GeoNode(Node node) {
      this.node = node;
    }
  }

}
