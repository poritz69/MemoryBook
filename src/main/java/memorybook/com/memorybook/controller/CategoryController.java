package memorybook.com.memorybook.controller;

import memorybook.com.memorybook.entity.AppUser;
import memorybook.com.memorybook.entity.Book;
import memorybook.com.memorybook.entity.Category;
import memorybook.com.memorybook.repository.AppUserRepository;
import memorybook.com.memorybook.repository.BookRepository;
import memorybook.com.memorybook.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class CategoryController {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private AppUserRepository appUserRepository;

    // Alle Kategorien anzeigen
    @GetMapping("/categories")
    public String allCategory(Principal principal, Model model) {
        model.addAttribute("categories", categoryRepository.findByOwnerOrderByNameAsc(currentUser(principal)));
        return "categories/list";
    }

    // Kategorie anlegen
    @PostMapping("/save/category-list")
    public String createCategory(@RequestParam String name, Principal principal, RedirectAttributes redirect) {
        AppUser user = currentUser(principal);
        name = name.trim();
        if (name.isEmpty() || name.length() > 60) {
            redirect.addFlashAttribute("error", "Der Name muss 1 bis 60 Zeichen lang sein.");
        } else if (categoryRepository.existsByOwnerAndNameIgnoreCase(user, name)) {
            redirect.addFlashAttribute("error", "Diese Kategorie gibt es schon.");
        } else {
            Category category = new Category();
            category.setName(name);
            category.setOwner(user);
            categoryRepository.save(category);
            redirect.addFlashAttribute("message", "Kategorie angelegt.");
        }
        return "redirect:/categories";
    }

    // Ermöglicht Suche nach spezifischer Kategorie
    @GetMapping("/categories/{id}")
    public String getCategoryById(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("category", findCategory(id, currentUser(principal)));
        return "categories/detail";
    }

    // Kategorie umbenennen
    @PostMapping("/categories/rename/{id}")
    public String renameCategory(@PathVariable Long id, @RequestParam String name, Principal principal,
                                 RedirectAttributes redirect) {
        AppUser user = currentUser(principal);
        Category category = findCategory(id, user);
        name = name.trim();
        if (name.isEmpty() || name.length() > 60) {
            redirect.addFlashAttribute("error", "Der Name muss 1 bis 60 Zeichen lang sein.");
        } else if (!name.equalsIgnoreCase(category.getName()) && categoryRepository.existsByOwnerAndNameIgnoreCase(user, name)) {
            redirect.addFlashAttribute("error", "Diese Kategorie gibt es schon.");
        } else {
            category.setName(name);
            categoryRepository.save(category);
            redirect.addFlashAttribute("message", "Kategorie umbenannt.");
        }
        return "redirect:/categories/" + id;
    }

    // Kategorie löschen, die Bücher bleiben erhalten
    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        Category category = findCategory(id, currentUser(principal));
        // Erst aus den Büchern entfernen, weil die Zuordnung beim Buch gespeichert ist
        for (Book book : category.getBooks()) {
            book.getCategories().remove(category);
            bookRepository.save(book);
        }
        categoryRepository.delete(category);
        redirect.addFlashAttribute("message", "Kategorie gelöscht.");
        return "redirect:/categories";
    }

    private Category findCategory(Long id, AppUser user) {
        return categoryRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private AppUser currentUser(Principal principal) {
        return appUserRepository.findByUsername(principal.getName());
    }
}
