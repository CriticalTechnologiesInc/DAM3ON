-- phpMyAdmin SQL Dump
-- version 4.5.4.1deb2ubuntu2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jan 09, 2018 at 09:34 AM
-- Server version: 5.7.20-0ubuntu0.16.04.1
-- PHP Version: 7.0.22-0ubuntu0.16.04.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `nonce_db`
--

CREATE DATABASE IF NOT EXISTS nonce_db DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE nonce_db;

-- --------------------------------------------------------

--
-- Table structure for table `nonce_table`
--

CREATE TABLE `nonce_table` (
  `nonce` varchar(25) NOT NULL,
  `email` varchar(35) NOT NULL,
  `time` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `nonce_table`
--

INSERT INTO `nonce_table` (`nonce`, `email`, `time`) VALUES
('FOaEYSB3Zn1499967245', '16gG30Wl7gvy@critical.com', 1499967245),
('Y8gnBFiBue1501591533', '1NAQvmgBp4UJ@critical.com', 1501591533),
('req2gF8MBN1499967453', '24bpqiJSohkY@critical.com', 1499967453),
('sPtr608zmP1510936631', '2KtjigmCzraB@critical.com', 1510936631),
('kC56WTffd61501504734', '2yeVPMb9bRWR@critical.com', 1501504734),
('L8lNCdDvgW1499873051', '38aD4MuDkdee@critical.com', 1499873051),
('GXqrWSGhah1501104505', '44CsSw9l16BF@critical.com', 1501104505),
('CsFtSz0u221501504636', '8apeHeSYPrX3@critical.com', 1501504636),
('FlznmzSfIo1501615575', '8glkbvCbmljf@critical.com', 1501615575),
('XLxWr9diSR1510767848', 'advanced4@gmail.com', 1510767848),
('gAcFWK7U4s1497445360', 'aes2rMKBcPRC@critical.com', 1497445360),
('3tpdb0UUJ81499953743', 'aMemUNO7Tki7@critical.com', 1499953743),
('h4BaG2MKzk1500933671', 'b1dXc8EeOw72@critical.com', 1500933671),
('3oiDzOAFgS1501104816', 'b453HcdBphf9@critical.com', 1501104816),
('Az98xYYZfM1501513746', 'brUg8wldcg8n@critical.com', 1501513746),
('zSYSFz67mA1500934293', 'BZLn7OwUKN4X@critical.com', 1500934293),
('XkZWZID7uA1504122474', 'c8Yf85n0Qyyf@critical.com', 1504122474),
('b62Jd9PrRy1500934420', 'CcHwrHd74CP5@critical.com', 1500934420),
('DIl0jlKW2k1509488669', 'clinger024@gmail.com', 1509488669),
('HN5zJ831rT1499965866', 'CLoOZAfTiS3v@critical.com', 1499965866),
('A0x8kapy1R1496854712', 'criticattest5@gmail.com', 1496854712),
('Fx2Vd6v20i1501537977', 'D2kkQRYW7WOZ@critical.com', 1501537977),
('4nSq3WTGHk1500933903', 'D9iFtCFPJTUU@critical.com', 1500933903),
('hW4dnbiHFX1500934605', 'dyURZabdLAw7@critical.com', 1500934605),
('uZQ3SAZZ6N1501524985', 'e1utxca4lqIt@critical.com', 1501524985),
('O3lUFu8fWm1501590763', 'E5L3N4tHI5wE@critical.com', 1501590763),
('HROMD0opGu1499965285', 'E8Y88AqoVrXs@critical.com', 1499965285),
('Ga3onKX8eB1501525380', 'ErP2KNTRNR6O@critical.com', 1501525380),
('DrGmWqrpGY1504119301', 'FcDfncFr65Ae@critical.com', 1504119301),
('P22yuq20x31501594513', 'FFs0QWpnLrGn@critical.com', 1501594513),
('YIRJbMc7Zw1509973893', 'fO45ELUPbSXj@critical.com', 1509973893),
('WpawQDMsUr1500933825', 'fPmyLdtpu58o@critical.com', 1500933825),
('AsuEdhzM491499875907', 'grz6G7FVHIBa@critical.com', 1499875907),
('6ucV2maGqw1500380083', 'gtTLStUeFMLK@critical.com', 1500380083),
('vBZGa2PgiN1500935959', 'GVlSgzcu05mG@critical.com', 1500935959),
('N3jJcyOg8Y1501105372', 'GwRFwR7EKqxy@critical.com', 1501105372),
('rflhB9HGki1501100676', 'hawkeye024@gmail.com', 1501100676),
('xxwIkUS6cW1500934219', 'HCQadDOl3Rhq@critical.com', 1500934219),
('ZhuksbjGuM1504117624', 'hPeSHkF7n3XY@critical.com', 1504117624),
('Iby2olbRLx1510767089', 'I2fSzNGo7h5k@critical.com', 1510767089),
('0TzIgcN0B11500935651', 'igzJftQF4nal@critical.com', 1500935651),
('hzUmfobedv1500933993', 'IYftHkmf4PEM@critical.com', 1500933993),
('VLhYP9Y2Vw1499965024', 'J5pBwyeKlksx@critical.com', 1499965024),
('1EZ20dVCvM1499871490', 'j7aNt7bpg5A9@critical.com', 1499871490),
('HlthMXB0p71499879068', 'jEkd2UVgL91k@critical.com', 1499879068),
('eoEC4vXEUY1501182390', 'jKAwRcoRVNGN@critical.com', 1501182390),
('ttPTh3lF6P1499967368', 'JLAUOQxG7wYQ@critical.com', 1499967368),
('ta85nloAK81500382704', 'JlpSCn6p1qQS@critical.com', 1500382704),
('iZl1re6a3C1510768239', 'john.doe@critical.com', 1510768239),
('3UvkSAPT8c1497389292', 'JrIkMcjDmlse@critical.com', 1497389292),
('WavSdHuPjf1500381309', 'jTaborsQlmxF@critical.com', 1500381309),
('FbY3GKqy111499964608', 'k6dzNIBIcm5R@critical.com', 1499964608),
('QkLrLCDlrm1501616857', 'lej4O1dE3z6L@critical.com', 1501616857),
('oa1ezw40HR1499873271', 'LxKWf8ovGCgj@critical.com', 1499873271),
('8s2DLKMBrQ1501593735', 'm0FAD7RN0Tcy@critical.com', 1501593735),
('CmmoeVErTX1501182690', 'mBT6M3P9Ps4h@critical.com', 1501182690),
('aO93A2da1T1501590598', 'mGxCe5eHRsMv@critical.com', 1501590598),
('MAhbpbT2Sk1501518798', 'n3jYZGFBW8rA@critical.com', 1501518798),
('DjmmBV99901497391102', 'nlY0GNxtYRlp@critical.com', 1497391102),
('qs8g89sr791501524385', 'O9xdVjDOvbfJ@critical.com', 1501524385),
('un479kXxSv1501589317', 'oFg1vNU1DGWN@critical.com', 1501589317),
('4zPTN0XoK21501524632', 'OGFpHMkQtkzl@critical.com', 1501524632),
('KVgnJwwJQt1499879983', 'OlZz3qAAbjXU@critical.com', 1499879983),
('P2pscboqzG1501161989', 'OvGdXH97AUmS@critical.com', 1501161989),
('hhbW7HqBcx1501182589', 'oxr00dvW1D8T@critical.com', 1501182589),
('djT9MdZR5h1509974621', 'p4JqkfevAdR0@critical.com', 1509974621),
('y6l9ODJNO41499880390', 'paaNcjgBktMw@critical.com', 1499880390),
('yabj69wvcq1497390114', 'PTvxxoYR4anT@critical.com', 1497390114),
('HxPCgnJM1u1499878906', 'PzqQUqk5i2IL@critical.com', 1499878906),
('KHrUgzrHRj1501525172', 'QkseS1QYSuYf@critical.com', 1501525172),
('GKbzlLjhko1497443270', 'qvmnL0FYo5AG@critical.com', 1497443270),
('S2JSykJhuD1499876169', 'riBU4YHgGo8f@critical.com', 1499876169),
('Y5pkCFVAkS1501524287', 's3jwb7YRQKty@critical.com', 1501524287),
('ExjkQt8fPT1500380307', 'sTb0Jpk2jKbr@critical.com', 1500380307),
('b61mgLUczt1500934641', 'T1pyPzUPPbVl@critical.com', 1500934641),
('TI8W90ZSSx1499880530', 'T2QSiyxtMzyT@critical.com', 1499880530),
('dTSiZswc0I1501104548', 'tvZLJc72UPb8@critical.com', 1501104548),
('S6tAzuYGJs1499953611', 'U0nj8mPTbfkO@critical.com', 1499953611),
('kqmYt6nPxX1501161942', 'uuaM4fxpT7iR@critical.com', 1501161942),
('QHoAB1rMtD1499869715', 'VDROM7qfYWC0@critical.com', 1499869715),
('Ydsqaq39jo1501105237', 'VfBPI3pqCw9a@critical.com', 1501105237),
('6XxUlMV8Of1501524792', 'vw6nDutG9Q9b@critical.com', 1501524792),
('ydzSLdsqbN1497621496', 'W0wx9aNZBdwO@critical.com', 1497621496),
('EEZ94ujRI11497389376', 'xQKzl6uk2fav@critical.com', 1497389376),
('MAZwKHdSis1501525541', 'XRidjc7B3Vce@critical.com', 1501525541),
('cZPgafbmTi1497621551', 'y0prHrAQHqiG@critical.com', 1497621551),
('V9sajBM3BC1500380974', 'ZK6KvI4Eujfl@critical.com', 1500380974),
('PIAdWYI1Zv1501590418', 'ZQh2krqoNzCU@critical.com', 1501590418),
('q8W75oaeSg1497390861', 'zrYEHf7sRhNc@critical.com', 1497390861);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `nonce_table`
--
ALTER TABLE `nonce_table`
  ADD PRIMARY KEY (`email`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
