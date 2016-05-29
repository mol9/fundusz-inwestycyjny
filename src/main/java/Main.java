import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;

import java.net.URI;
import java.net.URISyntaxException;

import static spark.Spark.*;
import spark.template.freemarker.FreeMarkerEngine;
import spark.ModelAndView;
import static spark.Spark.get;

import java.awt.Color;
import java.text.SimpleDateFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

import java.io.*;

import com.heroku.sdk.jdbc.DatabaseUrl;

public class Main {
	private static TimeSeriesCollection dataset= new TimeSeriesCollection();
	
	public static void main(String[] args) {

    port(Integer.valueOf(System.getenv("PORT")));
    staticFileLocation("/public");

    // get("/hello", (req, res) -> "Hello World");

    get("/", (request, response) -> {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("message", "Hello World!");

		return new ModelAndView(attributes, "index.ftl");
    }, new FreeMarkerEngine()
	);
	
	post("/",(request, response)->{
		Connection connection = null;
		Map<String, Object> attributes = new HashMap<>();
		try {
		connection = DatabaseUrl.extract().getConnection();
		Statement stmt = connection.createStatement();
		
		String start_date=request.queryParams("start-date");
		String end_date=request.queryParams("end-date");
		
		ResultSet rs = stmt.executeQuery("SELECT data,value FROM kurs WHERE data>'"+start_date+"'and data<'"+end_date+"';");

		ArrayList<String> output = new ArrayList<String>();
		TimeSeries s1= new TimeSeries("Trust A");
		while (rs.next()) {
			double ftmp=rs.getDouble("value");
			Date dtmp=rs.getDate("data");
			s1.add(new Day(dtmp),ftmp);
			output.add("<tr><td>"+dtmp+"</td><td>"+ftmp+"</td></tr>");
		}
		dataset= new TimeSeriesCollection();
		dataset.addSeries(s1);
		
		//JFreeChart chart = createChart(dataset);
		
		attributes.put("image_path", "/chart");
		attributes.put("results", output);
		return new ModelAndView(attributes, "index.ftl");
		} catch (Exception e) {
			attributes.put("message", "Wystąpił błąd: " + e);
			return new ModelAndView(attributes, "error.ftl");
		} finally {
		if (connection != null) try{connection.close();} catch(SQLException e){}
		}
	},new FreeMarkerEngine()
	);
	
	get("/chart", (request, response)->{ //render chart
			response.type("image/png");
			OutputStream outputStream = response.raw().getOutputStream();
			JFreeChart chart = createChart(dataset,"Kurs giełdowy Funduszu A", false);
			ChartUtilities.writeChartAsPNG(outputStream, chart, 1000, 500);
			return "";
		}
	);
			
    get("/compare", (req, res) -> {
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("compare", true);
			
			return new ModelAndView(attributes, "index.ftl");
		}, new FreeMarkerEngine()
	);
	
	
	post("/compare", (request, response) -> {
			Connection connection = null;
			Map<String, Object> attributes = new HashMap<>();
			attributes.put("compare", true);
			
			String start_quota=request.queryParams("start-quota");
			String intrest_rate=request.queryParams("intrest-rate");
			String start_date=request.queryParams("start-date");
			String end_date=request.queryParams("end-date");
			try{
				double sq=Double.parseDouble(start_quota);
				double ir=Double.parseDouble(intrest_rate);
				
				connection = DatabaseUrl.extract().getConnection();
				Statement stmt = connection.createStatement();
				
				ResultSet rs = stmt.executeQuery("SELECT data,value FROM kurs WHERE data>'"+start_date+"'and data<'"+end_date+"';");

				// ArrayList<String> output = new ArrayList<String>();
				TimeSeries s1= new TimeSeries("Trust A");
				TimeSeries s2= new TimeSeries("Lokata");
				boolean first=true;
				
				Double start_price=0.0;
				LocalDate s_date=LocalDate.now();
				while (rs.next()) {
					double vtmp=rs.getDouble("value");
					Date dtmp=rs.getDate("data");
					
					if(first){
						start_price=vtmp;
						s_date=LocalDate.parse(dtmp.toString());
						first=false;
					}				
					double diff=(vtmp-start_price)*sq;
					
					LocalDate ld=LocalDate.parse(dtmp.toString());
					double diff2=sq*Math.pow(1+ir/365/100,ChronoUnit.DAYS.between(s_date, ld));
					
					s1.add(new Day(dtmp),diff);
					s2.add(new Day(dtmp),diff2);
					// output.add("<tr><td>"+ChronoUnit.DAYS.between(s_date, ld)+"</td><td>"+diff2+"</td></tr>");
				}
				dataset= new TimeSeriesCollection();
				dataset.addSeries(s1);
				dataset.addSeries(s2);
				
				attributes.put("start_quota", sq);
				attributes.put("intrest_rate", ir);
				attributes.put("image_path", "/chart_compare");
				// attributes.put("results", output);
				return new ModelAndView(attributes, "index.ftl");
			} catch (Exception e) {
				attributes.put("message", "Wystąpił błąd: " + e);
				return new ModelAndView(attributes, "error.ftl");
			} finally {
				if (connection != null) try{connection.close();} catch(SQLException e){}
			}
		}, new FreeMarkerEngine()
	);
	
	get("/chart_compare", (request, response)->{ //render chart
			response.type("image/png");
			OutputStream outputStream = response.raw().getOutputStream();
			JFreeChart chart = createChart(dataset,"Porównanie", true);
			ChartUtilities.writeChartAsPNG(outputStream, chart, 1000, 500);
			return "";
		}
	);
	}

	
	private static JFreeChart createChart(XYDataset dataset,String title, boolean legend) {

       JFreeChart chart = ChartFactory.createTimeSeriesChart(
            title,  // title
            "Data",             // x-axis label
            "Wartość",   // y-axis label
            dataset,            // data
            legend,               // create legend?
            false,               // generate tooltips?
            false               // generate URLs?
        );

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(false);
            renderer.setBaseShapesFilled(false);
            renderer.setDrawSeriesLineAsPath(true);
        }

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));

        return chart;

	}

}
