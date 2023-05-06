

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.*;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

/**
 * Servlet implementation class CzechRegionsCbr
 */
public class CzechRegionsCbr extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CzechRegionsCbr() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Repository repo = new SPARQLRepository("http://localhost:8890/sparql/");
		repo.init();
		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
				+ "PREFIX xsd:  <https://www.w3.org/2001/XMLSchema#> \n"
				+ "PREFIX skos:  <http://www.w3.org/2004/02/skos/core#> \n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "\n"
				+ "# Calculate crude birth rate for each czech region. If a we cannot find numberOfPeople for a region, include it in the result set anyway.\n"
				+ "# Save as a simplified rdf view.\n"
				+ "CONSTRUCT {\n"
				+ "?czechBirthRegion <http://example.org/opendata/czech/births/measure/crudeBirthRate> ?crudeBirthRate ;\n"
				+ "    <http://example.org/opendata/czech/people/measure/numberOfPeople> ?numberOfPeople ;\n"
				+ "    <http://example.org/opendata/czech/births/measure/liveBirths> ?liveBirths ;\n"
				+ "    a ?type ;\n"
				+ "    skos:prefLabel ?czechBirthRegionName .\n"
				+ "\n"
				+ "} \n"
				+ "WHERE {\n"
				+ "?czechBirthObs <http://example.org/opendata/czech/births/dimension/refArea> ?czechBirthRegion ;\n"
				+ "    <http://example.org/opendata/czech/births/measure/liveBirths> ?liveBirths .\n"
				+ "OPTIONAL {\n"
				+ "?czechBirthRegion owl:sameAs/^<http://example.org/opendata/czech/people/dimension/refArea>/<http://example.org/opendata/czech/people/measure/numberOfPeople> ?numberOfPeople .\n"
				+ "}\n"
				+ "\n"
				+ "?czechBirthRegion skos:prefLabel ?czechBirthRegionName .\n"
				+ "\n"
				+ "BIND(xsd:decimal(?liveBirths) / xsd:decimal(?numberOfPeople) * 1000.0 AS ?crudeBirthRate)\n"
				+ "\n"
				+ "?czechBirthRegion a ?type .\n"
				+ "VALUES(?type ?label) {\n"
				+ "    (<https://data.cssz.cz/ontology/ruian/Stat> \"Stat\"@cs)\n"
				+ "    (<https://data.cssz.cz/ontology/ruian/Vusc> \"Kraj\"@cs)\n"
				+ "    (<https://data.cssz.cz/ontology/ruian/Okres> \"Okres\"@cs)\n"
				+ "}\n"
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
        response.getWriter().println("<h1>Porodnost za rok 2021</h1>");
        response.getWriter().println("<table><tr><th>Region</th><th>Porodnost na 1000 obyvatel</th><th>Pocet nazoreni</th><th>Pocet lidi</th></tr>");
		try (RepositoryConnection con = repo.getConnection()) {
			GraphQueryResult graphResult = con.prepareGraphQuery(query).evaluate();
			Model resultModel = QueryResults.asModel(graphResult);
			ValueFactory factory = SimpleValueFactory.getInstance(); 
			
			for (Resource region : resultModel.filter(null, SKOS.PREF_LABEL, null).subjects()) {
				response.getWriter().print("<tr" + Utils.CreateObjectAndSubject(region.stringValue()) + ">");			    
				
				Set<Value> names = resultModel.filter(region, SKOS.PREF_LABEL, null).objects();
				if (!names.isEmpty()) {
					String name = names.iterator().next().stringValue();
					response.getWriter().print("<td" + Utils.CreateLiteralProperty(SKOS.PREF_LABEL.stringValue()) + ">");
			        response.getWriter().print(name + "</td>");
				} else {
					 response.getWriter().print("<td>missing</td>");
				}
				
				
				String cbrsIRI = "http://example.org/opendata/czech/births/measure/crudeBirthRate";
				Set<Value> cbrs = resultModel.filter(region, factory.createIRI(cbrsIRI), null).objects();
				if (!cbrs.isEmpty()) {
					String cbr = cbrs.iterator().next().stringValue();
					response.getWriter().print("<td" + Utils.CreateLiteralProperty(cbrsIRI));
					response.getWriter().print(Utils.CreateDataType("xsd:decimal", null) + ">");
			        response.getWriter().print(cbr + "</td>");
				} else {
					 response.getWriter().print("<td>missing</td>");
				}
		        
				String liveBirthsIRI = "http://example.org/opendata/czech/births/measure/liveBirths";
				Set<Value> liveBirths = resultModel.filter(region, factory.createIRI(liveBirthsIRI), null).objects();
		        if (!liveBirths.isEmpty()) {
					String liveBirth = liveBirths.iterator().next().stringValue();
					response.getWriter().print("<td" + Utils.CreateLiteralProperty(liveBirthsIRI));
					response.getWriter().print(Utils.CreateDataType("xsd:integer", null) + ">");
			        response.getWriter().print(liveBirth + "</td>");
				} else {
					 response.getWriter().print("<td>missing</td>");
				}
		        
				String numberOfPeopleIRI = "http://example.org/opendata/czech/people/measure/numberOfPeople";
				Set<Value> peopleCounts = resultModel.filter(region, factory.createIRI(numberOfPeopleIRI), null).objects();
		        if (!peopleCounts.isEmpty()) {
					String peopleCount = peopleCounts.iterator().next().stringValue();
					response.getWriter().print("<td" + Utils.CreateLiteralProperty(numberOfPeopleIRI));
					response.getWriter().print(Utils.CreateDataType("xsd:integer", null) + ">");
			        response.getWriter().print(peopleCount + "</td>");
				} else {
					 response.getWriter().print("<td>missing</td>");
				}
				
		        response.getWriter().println("</tr>");
			}
		
			response.getWriter().println("</table>");
			response.getWriter().println("</div></body></html>");
		}
	}

}
