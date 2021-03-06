package com.example.ecommerce.controller.cart;

import com.example.ecommerce.model.cart.Cart;
import com.example.ecommerce.model.cart.ItemCart;
import com.example.ecommerce.model.dto.cart.ItemCartForm;
import com.example.ecommerce.model.notification.Notification;
import com.example.ecommerce.model.product.Product;
import com.example.ecommerce.model.user.User;
import com.example.ecommerce.service.cart.ICartService;
import com.example.ecommerce.service.itemcart.IItemCartService;
import com.example.ecommerce.service.notification.INotificationService;
import com.example.ecommerce.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/carts")
public class CartRestController {
    @Autowired
    private ICartService cartService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IItemCartService iItemCartService;

    @GetMapping
    public ResponseEntity<Iterable<Cart>> findAll(){
        return new ResponseEntity<>(cartService.findAll(), HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Cart> findById(@PathVariable Long id){
        Optional<Cart> currentCart = cartService.findById(id);
        if (!currentCart.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(currentCart.get(), HttpStatus.OK);
    }
    @PutMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody ItemCartForm itemCartForm) {
        if (itemCartForm.getProduct().getQuantity() < itemCartForm.getQuantity()){
            return new ResponseEntity<>("Sản phẩm không đủ số lượng", HttpStatus.NO_CONTENT);
        }
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String username = userDetails.getUsername();
        Optional<User> currentUser = userService.findByUsername(username);
        if(!currentUser.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Cart> currentCart = cartService.findByUser(currentUser.get());
        if (!currentCart.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Long totalMoney = currentCart.get().getTotalMoney();
        boolean check = false;
        for (int i = 0; i < currentCart.get().getItemCarts().size(); i++){
            ItemCart itemCart = currentCart.get().getItemCarts().get(i);
            Product product = itemCartForm.getProduct();
            if (itemCart.getProduct().getId() == product.getId() && itemCart.getStatus() == false){
                Long money = itemCart.getQuantity() * itemCart.getProduct().getPrice();
                totalMoney -= money;
                Long quantity = itemCartForm.getQuantity();
                itemCart.setQuantity(quantity);
                money = product.getPrice() * quantity;
                totalMoney += money;
                itemCart.setDate(LocalDate.now());
                currentCart.get().setTotalMoney(totalMoney);
                check = true;
            }
            itemCart.setCart(currentCart.get());
            iItemCartService.save(itemCart);
        }
        if (check){
            return new ResponseEntity<>(cartService.save(currentCart.get()), HttpStatus.ACCEPTED);
        }
        ItemCart itemCart = iItemCartService.save(new ItemCart());
        itemCart.setCart(currentCart.get());
        itemCart.setComment(itemCartForm.getComment());
        itemCart.setProduct(itemCartForm.getProduct());
        itemCart.setQuantity(itemCartForm.getQuantity());
        itemCart.setStatus(false);
        itemCart.setDate(LocalDate.now());
        currentCart.get().getItemCarts().add(itemCart);
        currentCart.get().setTotalMoney(currentCart.get().getTotalMoney()+(itemCartForm.getProduct().getPrice() * itemCartForm.getQuantity()));
        return new ResponseEntity<>(cartService.save(currentCart.get()), HttpStatus.ACCEPTED);
    }
    @GetMapping("/by-user")
    public ResponseEntity<Cart> findByUser(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String username = userDetails.getUsername();
        Optional<User> currentUser = userService.findByUsername(username);
        if(!currentUser.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Cart> currentCart = cartService.findByUser(currentUser.get());
        if (!currentCart.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(currentCart.get(), HttpStatus.OK);
    }
    @PutMapping("/item/{id}")
    public ResponseEntity<Cart> deleteItemCartById(@PathVariable Long id){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        String username = userDetails.getUsername();
        Optional<User> currentUser = userService.findByUsername(username);
        if(!currentUser.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Cart> currentCart = cartService.findByUser(currentUser.get());
        if (!currentCart.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<ItemCart> currentItemCart = iItemCartService.findById(id);
        if (!currentItemCart.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        long money = currentCart.get().getTotalMoney() - (currentItemCart.get().getQuantity() * currentItemCart.get().getProduct().getPrice());
        currentCart.get().setTotalMoney(money);
        currentCart.get().getItemCarts().remove(currentItemCart.get());
        iItemCartService.deleteById(id);
        cartService.save(currentCart.get());
        return new ResponseEntity<>(currentCart.get(), HttpStatus.OK);
    }
}
