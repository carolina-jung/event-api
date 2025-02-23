package br.com.nlw.events.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.nlw.events.dto.SubscriptionRankingByUser;
import br.com.nlw.events.dto.SubscriptionRankingItem;
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
		
		User userRec = userRepo.findByEmail(user.getEmail());
		if (userRec == null) {
			userRec = userRepo.save(user);
		}
		
		User indicador = null;
		if (userId != null) {
			indicador = userRepo.findById(userId).orElse(null);
			if (indicador == null) {
				throw new UserIndicatorNotFoundException("Usuário indicador "+userId+" não existe.");
			}
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
	
	public List<SubscriptionRankingItem> getCompleteRanking(String prettyName){
		Event evt = eventRepo.findByPrettyName(prettyName);
		if (evt == null) {
			throw new EventNotFoundException("Evento "+prettyName+" não existe.");
		}
		
		return subsRepo.generateRanking(evt.getEventId());
	}
	
	public SubscriptionRankingByUser getRankingByUser(String prettyName, Integer userId) {
		List<SubscriptionRankingItem> ranking = getCompleteRanking(prettyName);
		
		SubscriptionRankingItem item = ranking.stream().filter(i -> i.userId().equals(userId)).findFirst().orElse(null);
		if (item == null) {
			throw new UserIndicatorNotFoundException("Não há incrições com indicações do usuário "+userId+" .");
		}
		
		Integer position = IntStream.range(0,ranking.size())
				.filter(pos -> ranking.get(pos).userId().equals(userId))
				.findFirst().getAsInt();
		
		return new SubscriptionRankingByUser(item, position+1);
	}

}
