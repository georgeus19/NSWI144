

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

/**
 * Servlet implementation class CzechRegionServlet
 */
public class CzechRegionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CzechRegionServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Repository repo = new SPARQLRepository("http://localhost:8890/sparql/");
		repo.init();
		
		String obsParam = request.getParameter("obs");
		if (obsParam == null) {
			obsParam = "967523873";
		}

		String queryString = "PREFIX xsd:  <https://www.w3.org/2001/XMLSchema#> \n"
				+ "PREFIX skos:  <http://www.w3.org/2004/02/skos/core#> \n"
				+ "\n"
				+ "# Get UK regions linked by live births similarity for Plzeň-město - updated to get more data in web app.\n"
				+ "SELECT ?czechRegion ?czechRegionName ?czLiveBirths ?ukRegion ?ukRegionName ?ukLiveBirths ?liveBirthsDifference ?liveBirthsDifferenceLessThanTen\n"
				+ "WHERE {\n"
				+ "<http://example.org/opendata/czech/births/observation/" + obsParam + "> <http://example.org/opendata/czech/births/measure/liveBirths> ?czLiveBirths ;\n"
				+ "    <http://example.org/opendata/similar-live-births> ?ukLiveBirthsObservation ;\n"
				+ "    <http://example.org/opendata/czech/births/dimension/refArea> ?czechRegion .\n"
				+ "\n"
				+ "?czechRegion skos:prefLabel ?czechRegionName .\n"
				+ "\n"
				+ "?ukLiveBirthsObservation <http://example.org/opendata/uk/births/dimension/refArea> ?ukRegion ;\n"
				+ "    <http://example.org/opendata/uk/births/measure/liveBirths> ?ukLiveBirths .\n"
				+ "\n"
				+ "?ukRegion skos:prefLabel ?ukRegionName .\n"
				+ "\n"
				+ "BIND(ABS(xsd:integer(?czLiveBirths) - xsd:integer(?ukLiveBirths)) AS ?liveBirthsDifference) .\n"
				+ "BIND(IF(?liveBirthsDifference < 10, true, false) AS ?liveBirthsDifferenceLessThanTen) .\n"
				+ "}\n"
				+ "";
		
		response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
		response.getWriter().println("<html lang=cs>");
		response.getWriter().println("<style>\n"
				+ "table, th, td {\n"
				+ "  border:1px solid black;\n"
				+ "}\n"
				+ "</style>");

		response.getWriter().println("<body prefix=\"xsd: https://www.w3.org/2001/XMLSchema#\"><div prefix=\"skos: http://www.w3.org/2004/02/skos/core#\">");
		response.getWriter().println(Utils.CreateMenu());
		
		try (RepositoryConnection con = repo.getConnection()) {
		    TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		    TupleQueryResult result = tupleQuery.evaluate();
		   
		    List<BindingSet> bindings = result.stream().toList();
		   
		    BindingSet firstLine = bindings.get(0);
		    
		    Value searchedRegion = firstLine.getValue("czechRegion");
		    response.getWriter().print("<div" + Utils.CreateSubject(searchedRegion.stringValue()) + ">" );
		    
		    Value searchedRegionName = firstLine.getValue("czechRegionName");
		    response.getWriter().print("<h1" + Utils.CreateLiteralProperty("skos:prefLabel") + ">" );
		    response.getWriter().print(searchedRegionName.stringValue() + "</h1>");

		    Value searchedRegionBirths = firstLine.getValue("czLiveBirths");
		    response.getWriter().print("LiveBirths: <span" + Utils.CreateLiteralProperty("http://example.org/opendata/czech/births/measure/liveBirths"));
	    	response.getWriter().print(Utils.CreateDataType("xsd:integer", null) + ">" );
	    	response.getWriter().print(searchedRegionBirths.stringValue() + "</span>");		 
		    
		    response.getWriter().println("<table><tr><th>Region</th><th>Pocet narozenych v 2021</th><th>Rozdil oproti ceskemu regionu</th></tr>");
		    for (BindingSet binding : bindings) {
		    	Value region = binding.getValue("ukRegion");
		        response.getWriter().print("<tr lang=en" + Utils.CreateObjectAndSubject(region.stringValue()));
		        response.getWriter().println(Utils.CreatePropertyToEntity("http://example.org/opendata/similar-live-births") + ">");
		       
		        Value regionName = binding.getValue("ukRegionName");
		        response.getWriter().print("<td" + Utils.CreateLiteralProperty("skos:prefLabel") + ">");
		        response.getWriter().print(regionName.stringValue() + "</td>");
		        
		        Value ukLiveBirths = binding.getValue("ukLiveBirths");
		        response.getWriter().print("<td" + Utils.CreateLiteralProperty("http://example.org/opendata/uk/births/measure/liveBirths"));
		        response.getWriter().print(Utils.CreateDataType("xsd:integer", null) + ">");
		        response.getWriter().print(ukLiveBirths.stringValue() + "</td>");
		        
		        Value liveBirthsDifference = binding.getValue("liveBirthsDifference");
		        response.getWriter().print("<td" + Utils.CreateLiteralProperty("http://example.org/opendata/liveBirthsDifference"));
		        response.getWriter().print(Utils.CreateDataType("xsd:integer", null) + ">");
		        response.getWriter().print(liveBirthsDifference.stringValue() + "</td>");
		       
		        response.getWriter().println("</tr>");
		    }
		    response.getWriter().println("</table>");
		    response.getWriter().println("</div>");
		    response.getWriter().println("</div></body></html>");
		}
		
	}

}
