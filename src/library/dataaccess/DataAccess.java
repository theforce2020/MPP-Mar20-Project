package library.dataaccess;

import library.model.*;

import java.util.HashMap;

public interface DataAccess {

    HashMap<String, LibraryMember> loadMemberMap();

    void saveNewMember(LibraryMember member);

    void saveNewBook(Book book);

    HashMap<String, CheckoutRecord> loadCheckoutRecordMap();

    void saveNewCheckoutRecord(CheckoutRecord checkoutRecord);

    Book getBook(String isbn);

    void updateBook(Book bk);

    void deleteMember(String memberId);

    boolean doesMemberExist(String memberId);

    Librarian getLibrarianById(int librarianId);

    boolean isBookAvailable(String isbn);

    void saveLibrarian(Librarian librarian);

    void updateLibrarian(Librarian librarian);

    HashMap<String, Book> loadBookMap();

    void updateLibraryMember(LibraryMember member);

    Admin getAdminById(int adminId);

    SystemUser getSystemUser(String username);

    LibraryMember getLibraryMember(String memberId);
}