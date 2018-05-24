/** Proyecto: Juego de la vida.
 *  Resuelve todos los aspectos relacionados con la presentación 
 *  de una simulación. 
 *  Colabora en el patron MVC
 *  @since: prototipo2.1
 *  @source: VistaSimulacionTexto.java 
 *  @version: 2.1 - 2017.05.17
 *  @author: ajp
 */

package accesoUsr.consola;

import java.io.Console;
import java.util.Scanner;

import accesoUsr.OperacionesVista;
import accesoUsr.consola.control.ControlSimulacion;
import config.Configuracion;

public class VistaSimulacion implements OperacionesVista {
	
	final int CICLOS = new Integer(Configuracion.get().getProperty("simulacion.ciclos"));
	private Console consola;
	
	public VistaSimulacion() {
		consola = System.console();
	}

	/**
	 * Despliega en la consola el estado almacenado correspondiente
	 * a una generación del Juego de la vida.
	 */
	public void mostrarSimulacion(ControlSimulacion controlSimulacion) {
		byte[][] espacio = controlSimulacion.getMundo().getEspacio();
		for (int i = 0; i < espacio.length; i++) {
			for (int j = 0; j < espacio.length; j++) {
				this.mostrarSimple((espacio[i][j] == 1) ? "|o" : "| ");
			}
			this.mostrarMensaje("|");
		}
	}

	private void mostrarSimple(String mensaje) {
		if (consola != null) {
			consola.writer().print(mensaje);
			return;
		}
		System.out.print(mensaje);
	}
	
	public void confirmar() {
		mostrarMensaje("\nSimulación completada. "
				+ "\nPulsa intro para seguir...");
		if (consola != null) {
			consola.readLine();
			return;
		}
		// Desde entorno Eclipse la consola falla.
		new Scanner(System.in).nextLine();
	}

	@Override
	public void mostrarMensaje(String mensaje) {
		mostrarSimple(mensaje);
		
	}
}
