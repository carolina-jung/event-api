package br.com.nlw.events.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.nlw.events.dto.ErrorMessage;
import br.com.nlw.events.dto.SubscriptionResponse;
import br.com.nlw.events.exception.EventNotFoundException;
import br.com.nlw.events.exception.SubscriptionConflictException;
import br.com.nlw.events.exception.UserIndicatorNotFoundException;
import br.com.nlw.events.model.User;
import br.com.nlw.events.service.SubscriptionService;

@RestController
public class SubscriptionController {
	
	@Autowired
	private SubscriptionService service;
	
	@PostMapping({"/subscription/{prettyName}", "/subscription/{prettyName}/{userId}"})
	public ResponseEntity<?> createSubscription(
			@PathVariable String prettyName, 
			@RequestBody User user, 
			@PathVariable(required=false) Integer userId ){
		try {
			SubscriptionResponse res = service.createNewSubscription(prettyName, user, userId);
		
			if (res != null) {
				return ResponseEntity.ok(res);
				
			}
			
		} catch(EventNotFoundException exception) {
			return ResponseEntity.status(404).body(new ErrorMessage(exception.getMessage()));
		} catch(SubscriptionConflictException exception) {
			return ResponseEntity.status(409).body(new ErrorMessage(exception.getMessage()));
		} catch(UserIndicatorNotFoundException exception) {
			return ResponseEntity.status(404).body(new ErrorMessage(exception.getMessage()));
		}
		
		return ResponseEntity.badRequest().build();
		
	}

}
