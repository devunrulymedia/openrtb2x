/*
 * Copyright (c) 2010, The OpenRTB Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 *   3. Neither the name of the OpenRTB nor the names of its contributors
 *      may be used to endorse or promote products derived from this
 *      software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.openrtb.dsp.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.AvroRemoteException;
import org.openrtb.common.api.v_2_1.*;
import org.openrtb.dsp.intf.model.RTBAdvertiser;
import org.openrtb.dsp.intf.model.RTBRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenRTBAPIDummyTest implements OpenRTBAPI {
	private final Logger logger = LoggerFactory
			.getLogger(OpenRTBAPIDummyTest.class);
	private long lastBidNum;
	private final String adId = "AD123456789";

	public OpenRTBAPIDummyTest() {
		lastBidNum = 1L;
	}

	@Override
	public BidResponse process(BidRequest request) throws AvroRemoteException {
		BidResponse response = null;
		if (validateRequest(request)) {
			RTBRequestWrapper wReq = (RTBRequestWrapper) request;
			response = new BidResponse();
			response.id = wReq.getId();
			response.bidid = "simple-bid-tracker";
			Map<String, String> seats = wReq.getUnblockedSeats(wReq
					.getSSPName());
			for (Impression i : wReq.getRequest().getImp()) {
				for (Map.Entry<String, String> s : seats.entrySet()) {

					RTBAdvertiser a = wReq.getAdvertiser(s.getValue());
					SeatBid seat_bid = new SeatBid();
					seat_bid.seat = s.getKey();
					seat_bid.bid = new ArrayList<Bid>();
					Bid b = new Bid();
					b.id = "SimpleBid#" + lastBidNum++;
					b.impid = i.getId();

					b.price = i.getBidfloor() + (float) 0.10; // always bid 10
															// cents more than the floor
					b.nurl = a.getNurl();
					b.adid = adId; // serves up the same ad to all impressions

					seat_bid.bid.add(b);
					List<SeatBid> list = new ArrayList<SeatBid>();
					list.add(seat_bid);

					response.setSeatbid(list);
				}
			}
		}
		return response;
	}

	public boolean validateRequest(BidRequest request) {
		if (request == null) {
			logger.error("BidRequest object was null");
			return false;
		} else {
			boolean error = false;
			if (error = (request.getId() == null))
				logger.error("BidRequest must have valid Id");
			if (error = ((request.getImp() == null) || request.getImp()
					.isEmpty()))
				logger.error("BidRequest must have one or more impressions");
			if (error = ((request.getSite() == null) && (request.getApp() == null)))
				logger.error("BidRequest must have at least site or app object");
			if (error)
				return false;
		}
		return true;
	}
}
