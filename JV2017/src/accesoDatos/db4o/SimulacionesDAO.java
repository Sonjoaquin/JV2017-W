/** 
 * Proyecto: Juego de la vida.
 * Resuelve todos los aspectos del almacenamiento del DTO Simulacion. 
 * Utiliza base de datos db4o.
 * Colabora en el patron Fachada.
 * @since: prototipo2.0
 * @source: SimulacionesDAO.java 
 * @version: 2.1 - 2018.05.16
 * @author: ajp
 */

package accesoDatos.db4o;

import java.util.List;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;

import accesoDatos.DatosException;
import accesoDatos.OperacionesDAO;
import modelo.ModeloException;
import modelo.Mundo;
import modelo.SesionUsuario;
import modelo.Simulacion;
import modelo.Simulacion.EstadoSimulacion;
import modelo.Usuario;
import util.Fecha;

public class SimulacionesDAO implements OperacionesDAO {

	// Singleton 
	private static SimulacionesDAO instancia = null;

	// Elemento de almacenamiento. Base datos db4o
	private ObjectContainer db;

	/**
	 *  Método estático de acceso a la instancia única.
	 *  Si no existe la crea invocando al constructor interno.
	 *  Utiliza inicialización diferida.
	 *  Sólo se crea una vez; instancia única -patrón singleton-
	 *  @return instancia
	 */
	public static SimulacionesDAO getInstancia() {
		if (instancia == null) {
			instancia = new SimulacionesDAO();
		}
		return instancia;
	}

	/**
	 * Constructor por defecto de uso interno.
	 * Sólo se ejecutará una vez.
	 */
	private SimulacionesDAO() {
		db = Conexion.getDB();
		try {
			obtenerTodasMismoUsr("III1R");
		} 
		catch (DatosException e) {
			cargarPredeterminados();
		}
	}

	/**
	 *  Método para generar de datos predeterminados.
	 */
	private void cargarPredeterminados() {
		Simulacion simulacionDemo = null;
		try {
			// Obtiene usuario y mundo predeterminados.
			Usuario usrPredeterminado = UsuariosDAO.getInstancia().obtener("III1R");
			Mundo mundoPredeterminado = MundosDAO.getInstancia().obtener("Demo0");
			simulacionDemo = new Simulacion(usrPredeterminado, new Fecha(), mundoPredeterminado, EstadoSimulacion.PREPARADA);
			db.store(simulacionDemo);
		} 
		catch (DatosException e) {
			e.printStackTrace();
		}
	}

	/**
	 *  Cierra conexión.
	 */
	@Override
	public void cerrar() {
		db.close();
	}

	//Operaciones DAO
	/**	 
	 * Obtiene una simulación dado su id.
	 * @param idSimulacion - el idUsr+fecha de la Simulacion a buscar. 
	 * @return - la Simulacion encontrada.
	 * @throws DatosException - si no existe.
	 */	
	public Simulacion obtener(String idSimulacion) throws DatosException {	
		ObjectSet<Simulacion> result = null;
		Query consulta = db.query();
		consulta.constrain(Simulacion.class);
		consulta.descend("getIdSimulacion()").constrain(idSimulacion).equal();
		result = consulta.execute();
		if (result.size() > 0) {
			return result.get(0);
		}	
		else {
			throw new DatosException("Obtener: "+ idSimulacion + " no existe");
		}		
	}

	/**
	 * Obtiene una Sesion dado un objeto, reenvía al método que utiliza idSesion.
	 * @param obj - la Simulacion a buscar.
	 * @return - la Simulacion encontrada.
	 * @throws DatosException - si no existe.
	 */
	@Override
	public Simulacion obtener(Object obj) throws DatosException  {
		return this.obtener(((Simulacion) obj).getIdSimulacion());
	}

	/**
	 * Obtiene todos los objetos Simulacion almacenados.
	 * @return - la List de todas las encontradas.
	 */
	public List<Simulacion> obtenerTodos() {
		Query consulta = db.query();
		consulta.constrain(Simulacion.class);
		return consulta.execute();
	}

	/**
	 * Obtiene de todas las simulaciones por IdUsr de usuario.
	 * @param idUsr - el idUsr a buscar.
	 * @return - las simulaciones encontradas.
	 * @throws DatosException - si no existe ninguna.
	 */
	public List<Simulacion> obtenerTodasMismoUsr(String idUsr) throws DatosException {
		ObjectSet<Simulacion> result = null;
		Query consulta = db.query();
		consulta.constrain(Simulacion.class);
		consulta.descend("usr").descend("idUsr").constrain(idUsr).equal();
		result = consulta.execute();
		if (result.size() > 0) {
			return (List<Simulacion>) result;
		}
		else {
			throw new DatosException("Obtener: "+ idUsr + " no tiene simulaciones.");
		}	
	}

	/**
	 *  Alta de una nueva sin repeticiones según los idUsr + fecha.
	 *  @param obj - Simulación a almacenar.
	 *  @throws DatosException - si ya existe.
	 */	
	public void alta(Object obj) throws DatosException {
		assert obj != null;
		Simulacion simulacion = (Simulacion) obj;
		try {
			obtener(simulacion.getIdSimulacion());
		}
		catch (DatosException e) {
			db.store(simulacion);
			return;
		}
		throw new DatosException("Alta: " + simulacion.getIdSimulacion() + " ya existe.");
	}

	/**
	 * Elimina el objeto, dado el idUsr + fecha utilizado para el almacenamiento.
	 * @param idS - el idUsr + fecha de la Simulacion a eliminar.
	 * @return - la Simulacion eliminada.
	 * @throws DatosException - si no existe.
	 */
	@Override
	public Simulacion baja(String id) throws DatosException {	
		assert id != null;
		assert id != "";
		assert id != " ";
		try {
			Simulacion simulacion = obtener(id);
			db.delete(simulacion);
			return simulacion;
		}
		catch (DatosException e) {
			throw new DatosException("Baja: " + id + " no existe.");
		}
	}

	/**
	 *  Actualiza datos de una Simulacion reemplazando el almacenado por el recibido.
	 *	@param obj - Simulacion con las modificaciones.
	 *  @throws DatosException - si no existe.
	 */
	@Override
	public void actualizar(Object obj) throws DatosException {
		assert obj != null;
		Simulacion simulacionActualizada = (Simulacion) obj;
		Simulacion simulacionPrevia = null;
		try {	
			simulacionPrevia = obtener(simulacionActualizada.getIdSimulacion());
			simulacionPrevia.setUsr(simulacionActualizada.getUsr());
			simulacionPrevia.setMundo(simulacionActualizada.getMundo());
			simulacionPrevia.setFecha(simulacionActualizada.getFecha());
			simulacionPrevia.setEstado(simulacionActualizada.getEstado());
			db.store(simulacionPrevia);
		}
		catch (DatosException e) {
			throw new DatosException("Actualizar: " + simulacionActualizada.getIdSimulacion() + " no existe.");
		}
	}

	/**
	 * Obtiene el listado de todos las simulaciones almacenadas.
	 * @return el texto con el volcado de datos.
	 */
	@Override
	public String listarDatos() {
		StringBuilder listado = new StringBuilder();
		ObjectSet<Simulacion> result = null;
		Query consulta = db.query();
		consulta.constrain(Simulacion.class);	
		result = consulta.execute();
		if (result.size() > 0) {
			for (Simulacion simul: result) {
				listado.append("\n" + simul);
			}
			return listado.toString();
		}
		return null;
	}

	/**
	 * Obtiene el listado de todos los identificadores de simulaciones almacenadas.
	 * @return el texto con el volcado de datos.
	 */
	public String listarId() {
		StringBuilder listado = new StringBuilder();
		for (Simulacion simulacion: obtenerTodos()) {
			if (simulacion != null) {
				listado.append("\n" + simulacion.getIdSimulacion());
			}
		}
		return listado.toString();
	}

	/**
	 * Quita todos los objetos Simulacion de la base de datos.
	 */
	@Override
	public void borrarTodo() {
		// Elimina cada uno de los objetos obtenidos
		for (Simulacion simulacion: obtenerTodos()) {
			db.delete(simulacion);
		}
		cargarPredeterminados();
	}

} //class
