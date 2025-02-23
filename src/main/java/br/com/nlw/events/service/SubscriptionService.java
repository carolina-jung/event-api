package br.com.nlw.events.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.nlw.events.dto.SubscriptionResponse;
import br.com.nlw.events.exception.EventNotFoundException;
import br.com.nlw.events.exception.SubscriptionConflictException;
import br.com.nlw.events.exception.UserIndicatorNotFoundException;
import br.com.nlw.events.model.Event;
import br.com.nlw.events.model.Subscription;
import br.com.nlw.events.model.User;
import br.com.nlw.events.repository.EventRepository;
import br.com.nlw.events.repository.SubscriptionRepository;
import br.com.nlw.events.repository.UserRepository;

@Service
public class SubscriptionService {
	
	@Autowired
	private EventRepository eventRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private SubscriptionRepository subsRepo;
	
	public SubscriptionResponse createNewSubscription(String eventName, User user, Integer userId) {
		Event evt = eventRepo.findByPrettyName(eventName);
		if (evt == null) {
			throw new EventNotFoundException("Evento "+eventName+" não existe.");
		}
		
		User userRec = null;
		if (userId != null) {
			userRec = userRepo.findById(userId).orElse(null);
			if (userRec == null) {
				userRec = userRepo.save(user);
			}
		}
		
		User indicador = userRepo.findById(userId).orElse(null);
		if (indicador == null) {
			throw new UserIndicatorNotFoundException("Usuário indicador "+userId+" não existe.");
		}
		
		Subscription subs = new Subscription();
		subs.setEvent(evt);
		subs.setSubscriber(userRec);
		subs.setIndication(indicador);
		
		Subscription tmpSub = subsRepo.findByEventAndSubscriber(evt, userRec);
		if (tmpSub != null) {
			throw new SubscriptionConflictException("Usuário "+userRec.getName()+" já inscrito no evento "+evt.getTitle()+".");
		}
		
		Subscription res = subsRepo.save(subs);
		
		return new SubscriptionResponse(
				res.getSubscriptionNumber(), 
				"https://devstage.com/subscription/"+res.getEvent().getPrettyName()+"/"+res.getSubscriber().getId());
	}

}
