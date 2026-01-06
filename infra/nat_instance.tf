# --------------------------------------------------------
# NAT 인스턴스 생성 및 설정
# --------------------------------------------------------
resource "aws_eip" "muroom_nat_eip" {
  domain = "vpc"

  tags = {
    Name = "muroom-nat-eip"
  }
}

resource "aws_instance" "muroom_nat_instance" {
  ami                         = data.aws_ami.amazon_linux_2023.id
  instance_type               = "t4g.nano"
  subnet_id                   = aws_subnet.muroom_public_subnet_2a.id
  vpc_security_group_ids      = [aws_security_group.muroom_nat_sg.id]
  iam_instance_profile        = aws_iam_instance_profile.muroom_ssm_instance_profile.name
  source_dest_check           = false
  associate_public_ip_address = true

  user_data = <<-EOF
                #!/bin/bash
                dnf update -y
                dnf install -y iptables-services
                sysctl -w net.ipv4.ip_forward=1
                iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
                service iptables save
                systemctl enable iptables
                systemctl start iptables
              EOF

  tags = {
    Name = "muroom-nat-instance"
  }
}

resource "aws_eip_association" "muroom_nat_eip_association" {
  allocation_id = aws_eip.muroom_nat_eip.id
  instance_id   = aws_instance.muroom_nat_instance.id
}

# Private Subnet을 위한 라우팅 테이블에 NAT 인스턴스를 통한 경로 추가
resource "aws_route" "muroom_private_route_via_nat_instance" {
  route_table_id         = aws_route_table.muroom_private_route_table.id
  destination_cidr_block = "0.0.0.0/0"
  instance_id            = aws_instance.muroom_nat_instance.id
}
