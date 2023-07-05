package org.fffd.l23o6.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.mapper.TrainMapper;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.enum_.TrainType;
import org.fffd.l23o6.pojo.vo.train.AdminTrainVO;
import org.fffd.l23o6.pojo.vo.train.TrainVO;
import org.fffd.l23o6.pojo.vo.train.TicketInfo;
import org.fffd.l23o6.pojo.vo.train.TrainDetailVO;
import org.fffd.l23o6.service.TrainService;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.TrainSeatStrategy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import io.github.lyc8503.spring.starter.incantation.exception.CommonErrorType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {
    private final TrainDao trainDao;
    private final RouteDao routeDao;

    @Override
    public TrainDetailVO getTrain(Long trainId) {
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        return TrainDetailVO.builder().id(trainId).date(train.getDate()).name(train.getName())
                .stationIds(route.getStationIds()).arrivalTimes(train.getArrivalTimes())
                .departureTimes(train.getDepartureTimes()).extraInfos(train.getExtraInfos()).build();
    }

    @Override
    public List<TrainVO> listTrains(Long startStationId, Long endStationId, String date) {
        // TODO
        // First, get all routes contains [startCity, endCity]
        // Then, Get all trains on that day with the wanted routes
        if (startStationId.equals(endStationId)) {
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "出发站与到达站不可相同");
        }
        List<RouteEntity> routesContain = routeDao.findAll().stream()
                .filter((RouteEntity tmp)->tmp.getStationIds().contains(startStationId))
                .filter((RouteEntity tmp)->tmp.getStationIds().contains(endStationId))
                .toList();
        List<TrainEntity> trainsContain = trainDao.findAll();
        List<TrainEntity> ret = new ArrayList<>();
        for (TrainEntity tmp : trainsContain) {
            for (RouteEntity t : routesContain) {
                if (t.getId().equals(tmp.getRouteId()) && tmp.getDate().equals(date)) {
                    ret.add(tmp);
                }
            }
        }
        List<TrainVO> trainVOList = new ArrayList<>();
        for (TrainEntity train: ret) {
            RouteEntity route = routeDao.findById(train.getRouteId()).get();
            int startStationIndex = route.getStationIds().indexOf(startStationId);
            int endStationIndex = route.getStationIds().indexOf(endStationId);
            TrainVO trainVO = TrainVO.builder().id(train.getId()).name(train.getName())
                    .trainType(train.getTrainType().getText()).startStationId(startStationId).endStationId(endStationId)
                    .departureTime(train.getDepartureTimes().get(startStationIndex))
                    .arrivalTime(train.getArrivalTimes().get(endStationIndex))
                    .ticketInfo(createTicketInfos(train, startStationIndex, endStationIndex)).build();
            trainVOList.add(trainVO);
        }
        return trainVOList;
    }

    @Override
    public List<AdminTrainVO> listTrainsAdmin() {
        return trainDao.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(this::toAdminTrainVO).collect(Collectors.toList());
    }

    private AdminTrainVO toAdminTrainVO(TrainEntity train) {
        Long id = train.getId();
        String name = train.getName();
        String trainType = train.getTrainType().getText();
        Long routeId = train.getRouteId();
        String date = train.getDate();
        List<Date> departureTimes = train.getDepartureTimes();
        List<Date> arrivalTimes = train.getArrivalTimes();
        List<String> extraInfos = train.getExtraInfos();
        List<List<Double>> priceInfos = doubleArrayToList(train.getSeatPrices());
        return new AdminTrainVO(id, name, trainType, routeId, date, departureTimes, arrivalTimes, extraInfos,
                priceInfos);
    }

    @Override
    public void addTrain(String name, Long routeId, TrainType type, String date, List<Date> arrivalTimes,
            List<Date> departureTimes, List<List<Double>> seatPrices) {
        TrainEntity entity = TrainEntity.builder().name(name).routeId(routeId).trainType(type)
                .date(date).arrivalTimes(arrivalTimes).departureTimes(departureTimes).build();
        RouteEntity route = routeDao.findById(routeId).get();
        if (route.getStationIds().size() != entity.getArrivalTimes().size()
                || route.getStationIds().size() != entity.getDepartureTimes().size()) {
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "列表长度错误");
        }
        entity.setExtraInfos(new ArrayList<String>(Collections.nCopies(route.getStationIds().size(), "预计正点")));
        int stationCount = route.getStationIds().size();
        switch (entity.getTrainType()) {
            case HIGH_SPEED:
                entity.setSeats(GSeriesSeatStrategy.INSTANCE.initSeatMap(stationCount));
                entity.setSeatPrices(doubleListToArray(seatPrices));
                break;
            case NORMAL_SPEED:
                entity.setSeats(KSeriesSeatStrategy.INSTANCE.initSeatMap(stationCount));
                entity.setSeatPrices(doubleListToArray(seatPrices));
                break;
        }
        trainDao.save(entity);
    }

    @Override
    public void changeTrain(Long id, String name, Long routeId, TrainType type, String date, List<Date> arrivalTimes,
                            List<Date> departureTimes, List<List<Double>> seatPrices) {
        // TODO: edit train info, please refer to `addTrain` above
        TrainEntity entity = trainDao.findById(id).get().setName(name).setRouteId(routeId).setTrainType(type)
                .setDate(date).setArrivalTimes(arrivalTimes).setDepartureTimes(departureTimes);
        RouteEntity route = routeDao.findById(routeId).get();
        if (route.getStationIds().size() != entity.getArrivalTimes().size()
                || route.getStationIds().size() != entity.getDepartureTimes().size()) {
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "列表长度错误");
        }
        entity.setExtraInfos(new ArrayList<String>(Collections.nCopies(route.getStationIds().size(), "预计正点")));
        switch (entity.getTrainType()) {
            case HIGH_SPEED :
                entity.setSeats(GSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                entity.setSeatPrices(doubleListToArray(seatPrices));
                break;
            case NORMAL_SPEED :
                entity.setSeats(KSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
                entity.setSeatPrices(doubleListToArray(seatPrices));
                break;
        }
        trainDao.save(entity);
    }

    @Override
    public void deleteTrain(Long id) {
        trainDao.deleteById(id);
    }

    private List<TicketInfo> createTicketInfos(TrainEntity train, int start, int end) {
        Map<? extends TrainSeatStrategy.SeatType, Integer> leftSeatCount = null;
        TrainSeatStrategy.SeatType[] seatTypes = null;
        double[][] priceTable = train.getSeatPrices();
        switch (train.getTrainType()) {
            case HIGH_SPEED:
                leftSeatCount = GSeriesSeatStrategy.INSTANCE.getLeftSeatCount(start, end, train.getSeats());
                seatTypes = GSeriesSeatStrategy.GSeriesSeatType.values();
                break;
            case NORMAL_SPEED:
                leftSeatCount = KSeriesSeatStrategy.INSTANCE.getLeftSeatCount(start, end, train.getSeats());
                seatTypes = KSeriesSeatStrategy.KSeriesSeatType.values();
                break;
        }
        List<TicketInfo> ticketInfoList = new ArrayList<>();
        for (int type = 0; type < seatTypes.length; type++) {
            double price = 0;
            for (int i = start; i < end; i++) {
                price += priceTable[i][type];
            }
            TicketInfo ticketInfo = new TicketInfo(seatTypes[type].getText(), leftSeatCount.get(seatTypes[type]), price);
            ticketInfoList.add(ticketInfo);
        }
        return ticketInfoList;
    }

    private double[][] doubleListToArray(List<List<Double>> list) {
        int size0 = list.size(), size1 = list.get(0).size();
        double[][] arr = new double[size0][size1];
        for (int i = 0; i < size0; i++) {
            for (int j = 0; j < size1; j++) {
                arr[i][j] = list.get(i).get(j);
            }
        }
        return arr;
    }

    private List<List<Double>> doubleArrayToList(double[][] arr) {
        int size0 = arr.length, size1 = arr[0].length;
        List<List<Double>> listOut = new ArrayList<>();
        for (int i = 0; i < size0; i++) {
            List<Double> listIn = new ArrayList<>();
            for (int j = 0; j < size1; j++) {
                listIn.add(arr[i][j]);
            }
            listOut.add(listIn);
        }
        return listOut;
    }
}
