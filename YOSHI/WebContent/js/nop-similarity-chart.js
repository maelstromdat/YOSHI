var NOP_SIMILAR_BASIC = {
	
/*return google visualization data*/
	getvisualizationData : function(jsonData){
	
	 var   point1, dataArray = [],
	 
		   data = new google.visualization.DataTable();
		   
	       data.addColumn('string', 'Name');
	      
	       data.addColumn('number', 'Used languages');
	      
	       data.addColumn({type: 'string',role: 'tooltip','p': {'html': true}});
	       
	      /* for loop code for changing inputdata to 'data' of type google.visualization.DataTable*/
	      $.each(jsonData, function(i,obj){
	    	  
	    	  point1 =obj.name+" : "+obj.count+"";
	    	  
	    	  dataArray.push([obj.name, obj.count, NOP_SIMILAR_BASIC.returnTooltip(point1)]);
	      });
	      
	     data.addRows(dataArray);
	     
	     return data;
	},
	/*return options for bar chart: these options are for various configuration of chart*/
	getOptionForBarchart : function(){
		
		  var options = {
		  			animation:{
	       					 duration: 2000,
	       					 easing: 'out'
	     			  },
		  				
			          hAxis: {
			              baselineColor: '#ccc'
			          },
			          vAxis: {
			              baselineColor: '#ccc',
			              gridlineColor: '#fff'
			          },
			
			          isStacked: true,
			          height: 400,
			          backgroundColor: '#fff',
			          colors: ["#68130E", "#c65533"],
			          fontName: 'roboto',
			          fontSize: 12,
			          legend: {
			              position: 'top',
			              alignment: 'end',
			              textStyle: {
			                  color: '#b3b8bc',
			                  fontName: 'roboto',
			                  fontSize: 12
			              }
			          },
			          tooltip: {
			              isHtml: true,
			              showColorCode: true,
			              isStacked: true
			          }
	     		 };
		return   options;		 
		},
	/*Draws a Bar chart*/	
	drawBarChart : function (inputdata) {

		 var  barOptions = NOP_SIMILAR_BASIC.getOptionForBarchart(),
		
			  data = NOP_SIMILAR_BASIC.getvisualizationData(inputdata),
			  
			  chart = new google.visualization.ColumnChart(document.getElementById('nop-similarity-chart'));
			  
			  chart.draw(data, barOptions);
			  /*for redrawing the bar chart on window resize*/
		    $(window).resize(function () {
		    	
		        chart.draw(data, barOptions);
		    });
	 },
	/* Returns a custom HTML tooltip for Visualization chart*/
	 returnTooltip : function(dataPoint1){
	   
		 return "<div style='height:30px;width:150px;font:12px,roboto;padding:15px 5px 5px 5px;border-radius:3px;'>"+
				 "<span style='color:#68130E;font:12px,roboto;padding-right:20px;'>"+dataPoint1+"</span></div>";
	 },
	/*Makes ajax call to servlet and download data */
	getStudentData : function(){
		
			$.ajax({
			
				url: "NOPServlet",
				
				dataType: "JSON",
				
				success: function(data){
	
					NOP_SIMILAR_BASIC.drawBarChart(data);
				}
			});
	}
};	

google.load("visualization", "1", {packages:["corechart"]});
	
$(document).ready(function(){
	
	NOP_SIMILAR_BASIC.getStudentData();
});
