package library.business;

import java.util.List;

import library.exceptions.UsernameInUseException;
import library.model.Librarian;

public interface LibrarianInterface {
	
	public void saveLibrarian(String fName, String lName, String telephone, String street, String city, String state, String zip, String username, String password) throws UsernameInUseException;
	
	public void updateLibrarian(int librarianId, String fName, String lName, String telephone, String street, String city, String state, String zip);
	
	public List<Librarian> getAllLibrarians();
}