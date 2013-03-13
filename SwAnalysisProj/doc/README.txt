1.	 General
	This is a static analysis tool based on Soot infrastructure to analyze array accessing and warn about potential over flows.
	The tool analyzes java files ONLY.

2.	Prerequisites :
	a.	JRE 6 or higher
	b.	Eclipse version 3.7.2 (Not tested with higher versions)
	c.	Soot Eclipse Plugin – instructions of how to fetch and install the plugin can be found here http://www.sable.mcgill.ca/soot/eclipse/soot-eclipse-plugin-howto.html

3.	How to load and run the tool:
		a.	In Eclipse environment select “File->Import….” 
		b.	Select “From existing project…”
		c.	Select the directory into which you unzipped the project files.
		d.	Select “Finist”.
	This will open the project files in the left pane.
		e.	Choose one of the files in “tests” package.
		f.	Right click on the file and from the dropped menu select “Soot->Process Source File->Manage Configurations…”
		g.	Select “New”
		h.	Give a name to the configuration. Any name will do. And press OK
		i.	In the wizard opened make sure nothing is selected in the “General Options”
		j.	Select “Output options” in the left pane of the wizard.
		k.	Change “Output format” to “Jimple File”
		l.	Select “Soot Main Class”
		m.	In “Soot Main Class” type: arraycheck.MyMain
		n.	In “Soot Main Project” type: SwAnalysisProj
		o.	Press “Save”
		p.	Back in the “Configuration Menu” select the configuration name you just created and hit “Run”
		q.	From now on, this configuration will be available till the next restart of Eclipse. So no need to do steps g-o for another file you would like to examine.
	The results will be shown as highlights in the source file you chose to examine. Hovering with the mouse over the highlighted index access will display its meaning in a pop-up balloon. 

