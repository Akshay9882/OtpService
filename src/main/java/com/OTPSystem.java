/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@RestController
public class OTPSystem {
	private Map<String, reqtable> otp_data = new HashMap<>();
	private final static String ACCOUNT_SID = "HIDDEN";
	private final static String AUTH_ID = "HIDDEN";
	
	@Autowired
	private reqtableRepository dao;

	static {
		Twilio.init(ACCOUNT_SID, AUTH_ID);
	}

	@RequestMapping(value = "/getotp/{mobilenumber}/{expiry}/{mode}", method = RequestMethod.POST)
	public ResponseEntity<Object> sendOTP(@PathVariable("mobilenumber") String mobileno,
			@PathVariable("expiry") int expirytime, @PathVariable("mode") String mode) {
		java.util.Date utilDate = new java.util.Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(utilDate);
		cal.add(Calendar.SECOND, expirytime);
		long temp = cal.getTimeInMillis();
		reqtable req = new reqtable();
		req.setContact(mobileno);
		req.setOtp(String.valueOf(((int) (Math.random() * (10000 - 1000))) + 1000));
		req.setExpiry(String.valueOf(temp));
		req.setMode(mode);
		otp_data.put(mobileno, req);
		dao.save(req);
		if (mode.equals("SMS")) {
			Message.creator(new PhoneNumber("+919819159882"), new PhoneNumber("+HIDDEN"),"Your OTP is" + req.getOtp()).create();
			return new ResponseEntity<>("OTP is sent successfully", HttpStatus.OK);
		} else {
			System.out.println("SET 1:" + mode);
		}
		return null;
	}

	@GetMapping(value = "/verifyotp/{mobilenumber}/{otp}")
	public ResponseEntity<Object> verifyOTP(@PathVariable("mobilenumber") String mobilenumber,
			@PathVariable("otp") String otp) {
		if (otp == null || otp.length() <= 0) {
			return new ResponseEntity<>("Please provide OTP", HttpStatus.BAD_REQUEST);
		}
		List<reqtable> dataset = dao.findByContact(mobilenumber);
		if (dataset.size() > 0) {
			reqtable lastrow = dataset.stream().max(Comparator.comparing(reqtable::getId)).get();
			long tmp = System.currentTimeMillis();
			if (Long.valueOf(lastrow.getExpiry()) >= tmp) {
				if (lastrow.getOtp().equals(otp)) {
					return new ResponseEntity<>("OTP is verified successfully", HttpStatus.OK);
				}
				return new ResponseEntity<>("Invalid OTP", HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>("OTP is expired...", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Mobile number not found", HttpStatus.NOT_FOUND);
	}
}
