/******************************************************************************

Copyright 2011 Leandro Ferrari Thomaz

This file is part of Ensemble.

Ensemble is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ensemble is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Ensemble.  If not, see <http://www.gnu.org/licenses/>.

******************************************************************************/

package ensemble.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

// TODO: Auto-generated Javadoc
/**
 * The Class TestAutomation.
 */
public class TestAutomation {

	/** The ratio. */
	private static double[] ratio = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
	
	/** The num_agents. */
	private static int[] num_agents = {1, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400, 425, 450, 475, 500};
	
	/** The frame_size. */
	private static int[] frame_size = {250, 500, 750, 1000};

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
			
		DecimalFormat df = new DecimalFormat("0.0000");
		
		FileWriter res_file = null;

		try {

		res_file = new FileWriter("results.txt");
			
		
		int passes = 1;
		for (int r = 1; r < ratio.length; r++) {
			System.out.println("Ratio = " + df.format(ratio[r]));
			res_file.write("Ratio = " + df.format(ratio[r]) + "\n");
			res_file.flush();
			double result = Double.MIN_VALUE;
			for (int fs = 3; fs < frame_size.length; fs++) {
				if (result == 1.0) {
					break;
				}
				for (int na = 10; na < num_agents.length; na++) {
					try {
						BufferedReader xml = new BufferedReader(new InputStreamReader(new FileInputStream("dummy.xml")));
						StringBuffer buffer = new StringBuffer();
						String line;
						while ((line = xml.readLine()) != null) {
							if (line.matches(".*PERIOD=\".*\".*")) {
								int n1 = (int)(frame_size[fs] * ratio[r]);
								buffer.append((line.replaceFirst("PERIOD=\".*\"", "PERIOD=\""+ frame_size[fs] +" "+ n1 +" "+ frame_size[fs] +" "+"2000\""))+"\n");
							} else if (line.matches(".*QUANTITY=\".*\".*")) {
								buffer.append((line.replaceFirst("QUANTITY=\".*\"", "QUANTITY=\""+ num_agents[na] + "\""))+"\n");
							} else if (line.matches(".*SCHEDULER_THREADS=\".*\".*")) {
								buffer.append((line.replaceFirst("SCHEDULER_THREADS=\".*\"", "SCHEDULER_THREADS=\""+ (num_agents[na]<5?5:num_agents[na]) + "\""))+"\n");
							} else {
								buffer.append(line+"\n");
							}
						}
						BufferedWriter newXml = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("dummy.xml")));
						newXml.write(buffer.toString());
						newXml.close();
					
//						System.out.printf("ratio = %.1f - frame_size = %d - num_agents = %d\n", ratio[r], frame_size[fs], num_agents[na]);
						double partial_res = 0;
						for (int k = 0; k < passes; k++) {
							// Runs the simulation
							String cmd = "java -cp ./bin:./lib/jade.jar:./lib/NetUtil.jar -Djava.library.path=lib -XX:+ForceTimeHighResolution -Xms2g -Xmx2g ensemble.tools.Loader -f dummy.xml -nogui";
							Runtime rt = Runtime.getRuntime();
							Process p;
							p = rt.exec(cmd);
							BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));			
							p.waitFor();
							String last_line = null;
							while ((line = stdInput.readLine()) != null) {
								last_line = line;
							}
							partial_res += Double.valueOf(last_line);
//							System.out.printf("\tpartial result = %.4f\n", Double.valueOf(last_line));
						}
						result = partial_res/passes;
						res_file.write(df.format(result) + "\t");
						res_file.flush();
//						System.out.printf("\tresult = %.4f\n", partial_res/passes);
						System.out.print(df.format(result) + "\t");

						if (result < 0.05)
							break;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				System.out.println();
				res_file.write("\n");
				res_file.flush();
			}
		}

		res_file.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
