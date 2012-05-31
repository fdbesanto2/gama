package msi.gama.headless.runtime;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import msi.gama.headless.common.Globals;
import msi.gama.headless.common.HeadLessErrors;
import msi.gama.headless.core.*;
import msi.gama.headless.xml.Reader;
import msi.gama.headless.xml.XMLWriter;
import msi.gama.kernel.experiment.ParametersSet;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.MonitorOutput;
import msi.gama.outputs.OutputManager;
import msi.gama.runtime.GAMA;
import org.eclipse.equinox.app.*;

public class Application implements IApplication {

	public static boolean headLessSimulation = false;

	private static boolean isHeadlessSimulation() {
		return headLessSimulation;
	}

	private static void showLaunchingError()
	{
		System.out.println("Launching error... try again");
	}
	private static void checkParameters(String[] args)
	{
		if(args == null)
		{
			showError(HeadLessErrors.LAUNCHING_ERROR,null);
			System.exit(-1);
		}
		if(args.length < 2)
		{
			showError(HeadLessErrors.PARAMETER_ERROR,null);
			System.exit(-1);
		}
		Globals.OUTPUT_PATH=args[1];
		Globals.IMAGES_PATH=args[1]+"/snapshot";
		
		
		boolean success = ( new File(Globals.OUTPUT_PATH).exists());
		if (success) {
			showError(HeadLessErrors.EXIST_DIRECTORY_ERROR,Globals.OUTPUT_PATH);
			System.exit(-1);
		} 

		 success = ( new File(args[0]).exists());
		if (!success) {
			showError(HeadLessErrors.NOT_EXIST_FILE_ERROR,Globals.OUTPUT_PATH);
			System.exit(-1);
		} 

		
		success = ( new File(Globals.OUTPUT_PATH).mkdir());
		if (!success) {
			showError(HeadLessErrors.PERMISSION_ERROR,Globals.OUTPUT_PATH);
			System.exit(-1);
			} 
		success = (new File(Globals.IMAGES_PATH).mkdir());
		if (!success) {
			showError(HeadLessErrors.PERMISSION_ERROR,Globals.IMAGES_PATH);
			System.exit(-1);
		 } 
		
	//	System.out.println(HeadLessErrors.getError(errorCode, path)
	}

	private static void showError(int errorCode, String path)
	{
		System.out.println(HeadLessErrors.getError(errorCode, path));
		System.exit(-1);
	}
			
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	@Override
	public Object start(final IApplicationContext context) throws Exception {
		Map<String, String[]> mm=context.getArguments();
		String[] args = mm.get("application.args");
		checkParameters(args);
		
		Reader in=new Reader(args[0]);
		XMLWriter ou=new XMLWriter(Globals.OUTPUT_PATH+"/"+Globals.OUTPUT_FILENAME);
		in.parseXmlFile();
		Iterator<Simulation> it=in.getSimulation().iterator();
		while(it.hasNext())
		{
			Simulation si = it.next();
			try {
				si.setBufferedWriter(ou);
				si.loadAndBuild();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			si.play();
		}
			return null;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
