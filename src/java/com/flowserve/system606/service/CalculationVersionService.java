/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.flowserve.system606.service;

import com.flowserve.system606.model.Attribute;
import com.flowserve.system606.model.AttributeType;
import com.flowserve.system606.model.Contract;
import com.flowserve.system606.model.ContractAttachment;
import com.flowserve.system606.model.CurrencyAttribute;
import com.flowserve.system606.model.ExchangeRate;
import com.flowserve.system606.model.FinancialPeriod;
import com.flowserve.system606.model.Measurable;
import com.flowserve.system606.model.MetricStore;
import com.flowserve.system606.model.PerformanceObligation;
import com.flowserve.system606.model.TransientMeasurable;
import com.flowserve.system606.web.WebSession;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author shubhamv
 */
@Stateless
public class CalculationVersionService {

    private static final int SCALE = 14;

    @PersistenceContext(unitName = "rcscrPU")
    private EntityManager em;

    public void persist(Object object) {
        em.persist(object);
    }

    @Inject
    private PerformanceObligationService performanceObligationService;
    @Inject
    private ContractService contractService;
    @Inject
    private CalculationService calculationService;
    @Inject
    private AdminService adminService;
    @Inject
    private MetricService metricService;
    @Inject
    private AttributeService attributeService;
    @Inject
    private WebSession webSession;
    @Inject
    private CurrencyCache currencyCache;
    @Inject
    private FinancialPeriodService financialPeriodService;

//    private static String bucketName = "shubham-makedo";
//    private static String keyName = "Object-" + UUID.randomUUID();
    @PostConstruct
    public void init() {
        //metricTypes = metricService.findActiveMetricTypes();
    }

//    public AmazonS3 saveFileToS3(FileUploadEvent file) {
//
//        try {
//            String endpointUrl = getS3Details("endpointUrl");
//            String bucketName1 = getS3Details("bucketName");
//            String accessKey = getS3Details("accessKey");
//            String secretKey = getS3Details("secretKey");
//            Logger.getLogger(CalculationVersionService.class.getName()).log(Level.INFO, "accessKey : " + accessKey);
//            Logger.getLogger(CalculationVersionService.class.getName()).log(Level.INFO, "secretKey : " + secretKey);
//            AWSCredentials credentials;
//            credentials = new BasicAWSCredentials(accessKey, secretKey);
//            Logger.getLogger(CalculationVersionService.class.getName()).log(Level.INFO, "messagezz");
//            AmazonS3 s3client = new AmazonS3Client(credentials);
////            PutObjectResult putObj = s3client.putObject(new PutObjectRequest("shubham-makedo", "Jenkins", file).withCannedAcl(CannedAccessControlList.PublicRead));
////            Logger.getLogger(CalculationVersionService.class.getName()).log(Level.INFO, "putObj : " + putObj.toString());
//
//            S3Object s3Object = new S3Object();
//
//            ObjectMetadata omd = new ObjectMetadata();
//            omd.setContentType(file.getFile().getContentType());
//            omd.setContentLength(file.getFile().getSize());
//            omd.setHeader("filename", file.getFile().getFileName());
//
//            ByteArrayInputStream bis = new ByteArrayInputStream(file.getFile().getContents());
//
//            s3Object.setObjectContent(bis);
//            s3client.putObject(new PutObjectRequest(bucketName, keyName, bis, omd));
//            //s3Object.c
//        } catch (AmazonServiceException ase) {
//            System.out.println("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was "
//                    + "rejected with an error response for some reason.");
//
//            System.out.println("Error Message:    " + ase.getMessage());
//            System.out.println("HTTP Status Code: " + ase.getStatusCode());
//            System.out.println("AWS Error Code:   " + ase.getErrorCode());
//            System.out.println("Error Type:       " + ase.getErrorType());
//            System.out.println("Request ID:       " + ase.getRequestId());
//
//        } catch (AmazonClientException ace) {
//            System.out.println("Caught an AmazonClientException, which means the client encountered an internal error while "
//                    + "trying to communicate with S3, such as not being able to access the network.");
//
//        } catch (Exception e) {
//            Logger.getLogger(CalculationVersionService.class.getName()).log(Level.INFO, "new msg : " + e.getMessage());
//        }
//        return null;
//    }
//
//    public String getS3Details(String key) throws IOException {
//        Properties prop = new Properties();
//        InputStream input = null;
//        String filename = "/rcscr.properties";
//
//        input = AppInitializeService.class.getResourceAsStream("/resources/rcscr.properties");
//        Logger.getLogger(CalculationVersionService.class.getName()).log(Level.INFO, "message" + input);
//        prop.load(input);
//        String value = prop.getProperty(key);
//        return value;
//    }
    public ContractAttachment findContractAttachment(Long id) {
        Query query = em.createQuery("SELECT c FROM ContractAttachment c WHERE c.id = :ID", ContractAttachment.class);
        query.setParameter("ID", id);
        List<ContractAttachment> am = query.getResultList();
        if (am.size() > 0) {
            return am.get(0);
        }
        return null;
    }

    private boolean isMetricStoredAtMeasurable(AttributeType attributeType, Measurable measurable) {
        if (measurable instanceof MetricStore) {
            if (attributeType.isContractLevel() && measurable instanceof Contract) {
                return true;
            }
            if (attributeType.isPobLevel() && measurable instanceof PerformanceObligation) {
                return true;
            }
        }

        return false;

    }

    private boolean isEmptyTransientMesaurable(Measurable measurable) {
        if (measurable.getChildMeasurables().isEmpty() && measurable instanceof TransientMeasurable) {
            return true;
        }

        return false;
    }

    public void convertCurrency(Attribute attribute, Measurable measurable) throws Exception {
        if (!(attribute instanceof CurrencyAttribute)) {
            return;
        }
        if (!attribute.getMetricType().isConvertible()) {
            return;
        }

        CurrencyAttribute currencyMetricVersion = (CurrencyAttribute) attribute;
        FinancialPeriod period = webSession.getCurrentPeriod();

        if (currencyMetricVersion.getLcValue() == null && currencyMetricVersion.getCcValue() == null) {
            return;
        }

        if (attribute.getMetricType().getMetricCurrencyType() == null) {
            throw new IllegalStateException("There is no currency type defined for the metric type " + attribute.getMetricType().getId() + ".  Please contact a system administrator.");
        }
        if (measurable.getLocalCurrency() == null) {
            throw new IllegalStateException("There is no local currency defined.  Please contact a system administrator.");
        }
        if (measurable.getContractCurrency() == null) {
            throw new IllegalStateException("There is no contract currency defined.  Please contact a system administrator.");
        }
        if (measurable.getReportingCurrency() == null) {
            throw new IllegalStateException("There is no reporting currency defined.  Please contact a system administrator.");
        }

        if (currencyMetricVersion.isLocalCurrencyMetric()) {
            if (currencyMetricVersion.getLcValue() == null) {
                return;
            }
            if (BigDecimal.ZERO.equals(currencyMetricVersion.getLcValue())) {
                currencyMetricVersion.setCcValue(BigDecimal.ZERO);
                currencyMetricVersion.setRcValue(BigDecimal.ZERO);
            } else {
                currencyMetricVersion.setCcValue(convert(currencyMetricVersion.getLcValue(), measurable.getLocalCurrency(), measurable.getContractCurrency(), period.getLocalCurrencyRatePeriod()));
                currencyMetricVersion.setRcValue(convert(currencyMetricVersion.getLcValue(), measurable.getLocalCurrency(), measurable.getReportingCurrency(), period.getReportingCurrencyRatePeriod()));
            }
        } else if (currencyMetricVersion.isContractCurrencyMetric()) {
            if (currencyMetricVersion.getCcValue() == null) {
                return;
            }

            if (BigDecimal.ZERO.equals(currencyMetricVersion.getCcValue())) {
                currencyMetricVersion.setLcValue(BigDecimal.ZERO);
                currencyMetricVersion.setRcValue(BigDecimal.ZERO);
            } else {
                currencyMetricVersion.setLcValue(convert(currencyMetricVersion.getCcValue(), measurable.getContractCurrency(), measurable.getLocalCurrency(), period.getLocalCurrencyRatePeriod()));
                currencyMetricVersion.setRcValue(convert(currencyMetricVersion.getCcValue(), measurable.getContractCurrency(), measurable.getReportingCurrency(), period.getReportingCurrencyRatePeriod()));
            }
        }
    }

    private BigDecimal convert(BigDecimal amount, Currency fromCurrency, Currency toCurrency, FinancialPeriod period) throws Exception {

        ExchangeRate exchangeRate = getExchangeRate(fromCurrency, toCurrency, period);
        return amount.multiply(exchangeRate.getPeriodEndRate()).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
    }

    private ExchangeRate getExchangeRate(Currency fromCurrency, Currency toCurrency, FinancialPeriod period) throws Exception {
        ExchangeRate exchangeRate = null;
        try {
            String cacheKey = period.getId() + fromCurrency.getCurrencyCode() + toCurrency.getCurrencyCode();

            exchangeRate = currencyCache.getExchangeRate(cacheKey);

            //Logger.getLogger(CurrencyService.class.getName()).log(Level.INFO, "TestCache: hello: " + testCache.get("hello"));
            //Logger.getLogger(CurrencyService.class.getName()).log(Level.INFO, "Cache miss: " + cacheKey + " cache object ID: " + exchangeRateCache.toString());
            //exchangeRate = findRateByFromToPeriod(fromCurrency, toCurrency, period);
            //exchangeRateCache.put(cacheKey, exchangeRate);
            //exchangeRateCache.put(cacheKey, exchangeRate);
            if (exchangeRate == null) {
                throw new Exception("Missing exchange rate: " + cacheKey);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to find an exchange rate from " + fromCurrency.getCurrencyCode() + " to " + toCurrency.getCurrencyCode() + " in period " + period.getId());
        }

        return exchangeRate;
    }
}
