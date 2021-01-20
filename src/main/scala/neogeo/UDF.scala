package neogeo

import org.neo4j.procedure.{Description, Name, UserFunction}

class UDF {
  @UserFunction
  @Description("neogeo.something('hello') -> 'NeoGeo says: hello'")
  def something(
                 @Name("string") someString: String
               ): String = {
    f"NeoGeo says: ${someString}"
  }
}
