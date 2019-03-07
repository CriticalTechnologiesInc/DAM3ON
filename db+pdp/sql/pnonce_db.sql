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
-- Database: `pnonce_db`
--

CREATE DATABASE IF NOT EXISTS pnonce_db DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE pnonce_db;

-- --------------------------------------------------------

--
-- Table structure for table `pnonce_table`
--

CREATE TABLE `pnonce_table` (
  `subject` varchar(50) NOT NULL,
  `action` varchar(50) NOT NULL,
  `resource` varchar(700) NOT NULL,
  `decision` varchar(15) NOT NULL,
  `url` varchar(700) NOT NULL,
  `nonce` varchar(25) NOT NULL,
  `url_json` varchar(3000) DEFAULT NULL,
  `cap` varchar(1024) DEFAULT NULL,
  `atoken` varchar(20) DEFAULT NULL,
  `arg` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `pnonce_table`
--
ALTER TABLE `pnonce_table`
  ADD PRIMARY KEY (`subject`),
  ADD UNIQUE KEY `subject` (`subject`),
  ADD UNIQUE KEY `subject_2` (`subject`),
  ADD UNIQUE KEY `nonce` (`nonce`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
