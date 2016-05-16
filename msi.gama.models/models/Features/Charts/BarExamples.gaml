/**
 *  newtest
 *  Author: HPhi
 *  Description: 
 */

model newtest

global
{
}

experiment my_experiment type: gui {
	output {
		display "nice_bar_chart" {
			chart "Nice Cumulative Bar Chart" type:histogram 
			 	background: #darkblue
			 	color: #lightgreen 
			 	axes: #lightgreen
			 	title_font: 'Serif'
			 	title_font_size: 32.0
			 	title_font_style: 'italic'
			 	tick_font: 'Monospaced'
			 	tick_font_size: 14
			 	tick_font_style: 'bold' 
			 	label_font: 'Arial'
			 	label_font_size: 18
			 	label_font_style: 'bold' 
			 	legend_font: 'SanSerif'
			 	legend_font_size: 14
			 	legend_font_style: 'bold' 
			 	y_range:[-20,40]
			 	y_tick_unit:10
			 	x_label:'Nice Xlabel'
			 	y_label:'Nice Ylabel'
			 {
				data "BCC" value:10*cos(100*cycle)
					accumulate_values: true						
					style:stack
					color:#yellow;
				data "ABC" value:10*sin(100*cycle)
					accumulate_values: true						
					style: stack
					color:#blue;
				data "BCD" value:(cycle mod 10)
					accumulate_values: true						
					style: stack  
					marker_shape:marker_circle ;
			}
		} 
		display "data_cumulative_bar_chart" type:java2D {
			chart "data_cumulative_bar_chart" type:histogram 
			style:stack
			time_series:("cycle"+cycle)
			x_range:5
			{
				data "BCC" value:cos(100*cycle)*cycle*cycle
				accumulate_values: true						
				color:#yellow;
				data "ABC" value:cycle*cycle 
				accumulate_values: true						
					color:#blue;
				data "BCD" value:cycle+1
				accumulate_values: true						
				marker_shape:marker_circle ;
			}
		} 
 		
		display "data_non_cumulative_bar_chart" type:java2D {
			chart "data_non_cumulative_bar_chart" type:histogram 
			time_series: ["categ1","categ2"]
			style:"3d"
			series_label_position: xaxis
			{
				data "BCC" value:cos(100*cycle)*cycle*cycle
//				style:stack
				color:#yellow;
				data "ABC" value:cycle*cycle 
//				style: stack
					color:#blue;
				data "BCD" value:[cycle+1,cycle]
//				style: stack  
				marker_shape:marker_circle ;
			}
		} 
 		
		display "datalist_bar_cchart" type:java2D {
			chart "datalist_bar" type:histogram 
			series_label_position: onchart
			{
				datalist legend:["A","B","C"] 
					style: bar
					value:[cycle,cos(100*cycle),cos(100*(cycle+30))] 
					color:[#green,#blue,#red];
			}
		}

		display "onvalue_cumulative_bar_chart" type:java2D {
			chart "onvalue_cumulative_bar_chart" type:histogram 
			series_label_position: yaxis
			x_label: "my_time_label"
			{
				data "unique data value" 
					value:cos(cycle*10) 
					accumulate_values: true						
					color: #red;
			}
		}
		display "data_cumulative_style_chart" type:java2D {
			chart "Style Cumulative chart" type:histogram style:stack
			 	{ 
				data "Step" value:cos(100*cycle+40)
					accumulate_values: true						
					color:#blue;
				data "Bar" value:cos(100*cycle+60)
					accumulate_values: true						
					color:#green;
				data "Line" value:cos(100*cycle)
					accumulate_values: true						
					color:#orange;
				data "Dot" value:cos(100*cycle)*0.3
					accumulate_values: true						
					color:#red;
			}
		} 


	}
}