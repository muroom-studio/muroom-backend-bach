# --------------------------------------------------------
# VPC 생성
# --------------------------------------------------------
resource "aws_vpc" "muroom_vpc" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "muroom-vpc"
  }
}

# --------------------------------------------------------
# Subnet 생성 (Public Subnet 2개, Private Subnet 2개)
# --------------------------------------------------------
resource "aws_subnet" "muroom_public_subnet_2a" {
  vpc_id                  = aws_vpc.muroom_vpc.id
  cidr_block              = var.public_subnet_2a_cidr
  availability_zone       = "${data.aws_region.current.name}a"
  map_public_ip_on_launch = true

  tags = {
    Name = "muroom-public-subnet-2a"
  }
}

resource "aws_subnet" "muroom_public_subnet_2b" {
  vpc_id                  = aws_vpc.muroom_vpc.id
  cidr_block              = var.public_subnet_2b_cidr
  availability_zone       = "${data.aws_region.current.name}b"
  map_public_ip_on_launch = true

  tags = {
    Name = "muroom-public-subnet-2b"
  }
}

resource "aws_subnet" "muroom_private_subnet_2a" {
  vpc_id            = aws_vpc.muroom_vpc.id
  cidr_block        = var.private_subnet_2a_cidr
  availability_zone = "${data.aws_region.current.name}a"

  tags = {
    Name = "muroom-private-subnet-2a"
  }
}

resource "aws_subnet" "muroom_private_subnet_2b" {
  vpc_id            = aws_vpc.muroom_vpc.id
  cidr_block        = var.private_subnet_2b_cidr
  availability_zone = "${data.aws_region.current.name}b"

  tags = {
    Name = "muroom-private-subnet-2b"
  }
}

# --------------------------------------------------------
# Internet Gateway 및 Public Route Table 설정
# --------------------------------------------------------
resource "aws_internet_gateway" "muroom_igw" {
  vpc_id = aws_vpc.muroom_vpc.id

  tags = {
    Name = "muroom-igw"
  }
}

resource "aws_route_table" "muroom_public_route_table" {
  vpc_id = aws_vpc.muroom_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.muroom_igw.id
  }

  tags = {
    Name = "muroom-public-route-table"
  }
}

resource "aws_route_table_association" "muroom_public_subnet_2a_association" {
  subnet_id      = aws_subnet.muroom_public_subnet_2a.id
  route_table_id = aws_route_table.muroom_public_route_table.id
}

resource "aws_route_table_association" "muroom_public_subnet_2b_association" {
  subnet_id      = aws_subnet.muroom_public_subnet_2b.id
  route_table_id = aws_route_table.muroom_public_route_table.id
}

# --------------------------------------------------------
# NAT Gateway 및 Private Route Table 설정
# --------------------------------------------------------
resource "aws_route_table" "muroom_private_route_table" {
  vpc_id = aws_vpc.muroom_vpc.id

  tags = {
    Name = "muroom-private-route-table"
  }
}

resource "aws_route_table_association" "muroom_private_subnet_2a_association" {
  subnet_id      = aws_subnet.muroom_private_subnet_2a.id
  route_table_id = aws_route_table.muroom_private_route_table.id
}

resource "aws_route_table_association" "muroom_private_subnet_2b_association" {
  subnet_id      = aws_subnet.muroom_private_subnet_2b.id
  route_table_id = aws_route_table.muroom_private_route_table.id
}
